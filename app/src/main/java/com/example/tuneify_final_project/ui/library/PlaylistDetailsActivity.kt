package com.example.tuneify_final_project.ui.library

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.PlaylistSelectionAdapter
import com.example.tuneify_final_project.ui.adapters.SongAdapter
import com.example.tuneify_final_project.ui.models.Playlist
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class PlaylistDetailsActivity : AppCompatActivity() {

    private val songsList = mutableListOf<Song>()
    private lateinit var btnPlay: ImageView
    private lateinit var tvSongCount: TextView

    private var playlistId   = -1
    private var playlistName = ""
    private var coverFilename: String? = null

    private var currentUserId = -1

    // Used by the edit sheet to pick a new cover from gallery
    private var pendingCoverUri: Uri? = null
    private var editCoverPreview: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingCoverUri = result.data?.data
            pendingCoverUri?.let { uri ->
                editCoverPreview?.let { iv ->
                    Glide.with(this).load(uri).centerCrop().into(iv)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_details)

        PlaybackUtils.bind(this)
        MusicPlayerManager.addListener("PlaylistDetailsPlay") {
            runOnUiThread { syncPlayButton() }
        }

        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        playlistId   = intent.getIntExtra("PLAYLIST_ID", -1)
        playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: ""
        coverFilename = intent.getStringExtra("PLAYLIST_COVER")

        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        tvTitle.text = playlistName
        tvSongCount  = findViewById<TextView>(R.id.tv_song_count)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // Tap edit button to open edit sheet
        findViewById<ImageView>(R.id.btn_edit_playlist)
            .setOnClickListener { openEditSheet() }

        loadCover(coverFilename)

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_playlist_songs)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = SongAdapter(
            songsList,
            onSongClick = { song -> PlaybackUtils.playSong(this, song, songsList) },
            onMoreClick = { song -> openSongOptionsSheet(song) }
        )
        rv.adapter = adapter

        btnPlay = findViewById(R.id.btn_play_playlist)
        btnPlay.setOnClickListener {
            if (songsList.isEmpty()) return@setOnClickListener
            if (MusicPlayerManager.currentPlaylist === songsList) {
                MusicPlayerManager.togglePlayPause()
            } else {
                MusicPlayerManager.playPlaylist(songsList, 0)
            }
        }

        fetchSongs(adapter)
    }

    // ── Song count ────────────────────────────────────────────────────────────

    private fun updateSongCount() {
        val count = songsList.size
        tvSongCount.text = "$count song${if (count == 1) "" else "s"}"
    }

    // ── Cover loading ─────────────────────────────────────────────────────────

    private fun loadCover(filename: String?) {
        val iv = findViewById<ImageView>(R.id.iv_detail_cover)
        if (!filename.isNullOrEmpty() && filename != "null") {
            Glide.with(this)
                .load("http://${NetworkConfig.serverIp}:8000/covers/playlist/$filename")
                .placeholder(R.drawable.add_playlist_cover)
                .error(R.drawable.add_playlist_cover)
                .into(iv)
        } else {
            iv.setImageResource(R.drawable.add_playlist_cover)
        }
    }

    // ── Edit playlist bottom sheet ────────────────────────────────────────────

    private fun openEditSheet() {
        val sheet = BottomSheetDialog(this)
        val view  = layoutInflater.inflate(R.layout.layout_edit_playlist_sheet, null)

        editCoverPreview = view.findViewById(R.id.iv_edit_cover_preview)
        val etName = view.findViewById<EditText>(R.id.et_edit_playlist_name)
        val btnSave = view.findViewById<Button>(R.id.btn_save_playlist_edit)

        etName.setText(playlistName)
        loadCover(coverFilename) // show current cover in preview too
        if (!coverFilename.isNullOrEmpty() && coverFilename != "null") {
            Glide.with(this)
                .load("http://${NetworkConfig.serverIp}:8000/covers/playlist/$coverFilename")
                .into(editCoverPreview!!)
        }

        view.findViewById<TextView>(R.id.tv_change_cover).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()

            // 1. Save name if changed
            if (newName.isNotEmpty() && newName != playlistName) {
                val params = JSONObject()
                    .put("playlist_id", playlistId)
                    .put("new_name", newName)
                SocketManager.sendCommand("UPDATE_PLAYLIST_NAME", params) { response ->
                    if (response?.startsWith("OK") == true) {
                        playlistName = newName
                        runOnUiThread {
                            findViewById<TextView>(R.id.tv_detail_title).text = newName
                        }
                    }
                }
            }

            // 2. Upload new cover if picked
            pendingCoverUri?.let { uri ->
                uploadCover(uri, playlistId) { filename ->
                    coverFilename = filename
                    runOnUiThread { loadCover(filename) }
                    // Tell socket server to update DB
                    val params = JSONObject()
                        .put("playlist_id", playlistId)
                        .put("filename", filename)
                    SocketManager.sendCommand("UPDATE_PLAYLIST_COVER", params) {}
                }
                pendingCoverUri = null
            }

            sheet.dismiss()
        }

        sheet.setContentView(view)
        sheet.show()
    }

    private fun uploadCover(uri: Uri, playlistId: Int, onDone: (String) -> Unit) {
        Thread {
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@Thread
                val tempFile = File(cacheDir, "cover_upload_$playlistId.jpg")
                FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }

                val client = OkHttpClient()
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", tempFile.name,
                        tempFile.asRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url("http://${NetworkConfig.serverIp}:8000/upload_playlist_cover/$playlistId")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val json = JSONObject(response.body?.string() ?: "{}")
                if (json.getString("status") == "OK") {
                    onDone(json.getString("filename"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // ── Three-dot song options sheet ──────────────────────────────────────────

    private fun openSongOptionsSheet(song: Song) {
        val sheet = BottomSheetDialog(this)
        val view  = layoutInflater.inflate(R.layout.layout_song_options_sheet, null)

        view.findViewById<TextView>(R.id.tv_sheet_song_title).text = song.title

        // Remove from THIS playlist
        view.findViewById<View>(R.id.option_remove_from_playlist).setOnClickListener {
            val params = JSONObject()
                .put("playlist_id", playlistId)
                .put("song_id", song.id)
            SocketManager.sendCommand("REMOVE_SONG_FROM_PLAYLIST", params) { response ->
                if (response?.startsWith("OK") == true) {
                    runOnUiThread {
                        songsList.remove(song)
                        (findViewById<androidx.recyclerview.widget.RecyclerView>(
                            R.id.rv_playlist_songs).adapter as SongAdapter)
                            .updateList(songsList.toList())
                        updateSongCount()
                    }
                }
            }
            sheet.dismiss()
        }

        // Add to ANOTHER playlist
        view.findViewById<View>(R.id.option_add_to_playlist).setOnClickListener {
            sheet.dismiss()
            openPlaylistPickerSheet(song)
        }

        sheet.setContentView(view)
        sheet.show()
    }

    // Shows user's playlists so they can pick one to add the song to
    private fun openPlaylistPickerSheet(song: Song) {
        val pickerSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_add_to_playlist_sheet, null)
        val rv   = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_user_playlists)
        rv.layoutManager = LinearLayoutManager(this)

        val params = JSONObject().put("user_id", currentUserId)
        SocketManager.sendCommand("GET_USER_PLAYLISTS", params) { response ->
            if (response?.startsWith("OK|") == true) {
                val arr = JSONArray(response.substringAfter("OK|"))
                val playlists = mutableListOf<Playlist>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    playlists.add(Playlist(
                        o.getInt("id"), o.getString("name"),
                        o.getInt("user_id"), o.optString("cover_url", null)
                    ))
                }
                runOnUiThread {
                    rv.adapter = PlaylistSelectionAdapter(playlists) { chosen ->
                        val p = JSONObject()
                            .put("playlist_id", chosen.id)
                            .put("song_id", song.id)
                        SocketManager.sendCommand("ADD_SONG_TO_PLAYLIST", p) {}
                        pickerSheet.dismiss()
                    }
                }
            }
        }

        pickerSheet.setContentView(view)
        pickerSheet.show()
    }

    // ── Playback helpers ──────────────────────────────────────────────────────

    private fun syncPlayButton() {
        if (!::btnPlay.isInitialized) return
        btnPlay.setImageResource(
            if (MusicPlayerManager.isPlaying) R.drawable.pause_icon
            else R.drawable.play_icon
        )
    }

    override fun onResume() {
        super.onResume()
        NavigationUtils.setupBottomNav(this)
        PlaybackUtils.updateUI(this)
        syncPlayButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackUtils.unbind(this)
        MusicPlayerManager.removeListener("PlaylistDetailsPlay")
    }

    // ── Fetch songs ───────────────────────────────────────────────────────────

    private fun fetchSongs(adapter: SongAdapter) {
        val params = JSONObject().put("playlist_id", playlistId)
        SocketManager.sendCommand("GET_PLAYLIST_SONGS", params) { response ->
            if (response == null) return@sendCommand
            val start = response.indexOf("[")
            val end   = response.lastIndexOf("]") + 1
            if (start == -1 || end <= start) return@sendCommand
            val arr  = JSONArray(response.substring(start, end))
            val list = mutableListOf<Song>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Song(
                    o.getInt("id"), o.getString("title"),
                    o.getString("artist"), o.optString("cover_url", "")
                ))
            }
            runOnUiThread {
                songsList.clear()
                songsList.addAll(list)
                adapter.updateList(songsList.toList())
                updateSongCount()
                syncPlayButton()
            }
        }
    }
}