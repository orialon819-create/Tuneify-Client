package com.example.tuneify_final_project.ui.search

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.PlaylistSelectionAdapter
import com.example.tuneify_final_project.ui.adapters.SearchSongAdapter
import com.example.tuneify_final_project.ui.models.Playlist
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private lateinit var rvResults: RecyclerView
    private lateinit var searchAdapter: SearchSongAdapter
    private lateinit var etSearch: EditText
    private lateinit var ivClear: ImageView

    private val searchResults = mutableListOf<Song>()
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize Navigation
        NavigationUtils.setupBottomNav(this)

        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        rvResults = findViewById(R.id.rv_search_results)
        etSearch = findViewById(R.id.et_search_input)
        ivClear = findViewById(R.id.iv_clear_text)

        searchAdapter = SearchSongAdapter(
            searchResults,
            onSongClick = { song -> playSong(song) },
            onMoreClick = { song -> openPlaylistMenu(song) }
        )
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = searchAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                ivClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                if (query.length >= 2) performSearch(query) else {
                    searchResults.clear()
                    searchAdapter.updateList(searchResults)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        ivClear.setOnClickListener { etSearch.text.clear() }
    }

    override fun onResume() {
        super.onResume()
        // Essential: Refresh Nav and Bar state whenever returning to this screen
        NavigationUtils.setupBottomNav(this)
        PlaybackUtils.updateUI(this)
    }

    private fun performSearch(query: String) {
        val params = JSONObject().put("query", query)
        SocketManager.sendCommand("SEARCH_SONGS", params) { response ->
            if (response != null && response.startsWith("OK|")) {
                val rawJson = response.substringAfter("OK|")
                val songs = parseSongsFromJson(rawJson)

                runOnUiThread {
                    if (songs.isNotEmpty()) {
                        searchResults.clear()
                        searchResults.addAll(songs)
                        searchAdapter.updateList(songs)
                    } else {
                        searchResults.clear()
                        searchAdapter.updateList(mutableListOf())
                    }
                }
            }
        }
    }

    private fun playSong(song: Song) {
        try {
            // 1. Clean up existing global player
            MusicPlayerManager.mediaPlayer?.stop()
            MusicPlayerManager.mediaPlayer?.release()

            // 2. Sync data to the Global Manager
            MusicPlayerManager.currentSongTitle = song.title
            MusicPlayerManager.currentArtist = song.artist
            MusicPlayerManager.currentCoverUrl = song.coverUrl
            MusicPlayerManager.isPlaying = true

            val prefs = getSharedPreferences("MusicDebug", MODE_PRIVATE)
            prefs.edit().putString("last_title", song.title).apply()

            val encodedTitle = Uri.encode(song.title)
            val streamUrl = "http://${NetworkConfig.serverIp}:8000/stream/$encodedTitle"

            // 3. Set the Global MediaPlayer
            MusicPlayerManager.mediaPlayer = MediaPlayer().apply {
                setDataSource(streamUrl)
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                    // Update UI on main thread once playback starts
                    runOnUiThread { PlaybackUtils.updateUI(this@SearchActivity) }
                }
            }

            // Show the bar immediately
            PlaybackUtils.updateUI(this)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Playback Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlaylistMenu(song: Song) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_add_to_playlist_sheet, null)
        val rvPlaylists = view.findViewById<RecyclerView>(R.id.rv_user_playlists)
        rvPlaylists.layoutManager = LinearLayoutManager(this)

        val params = JSONObject().put("user_id", currentUserId)
        SocketManager.sendCommand("GET_USER_PLAYLISTS", params) { response ->
            if (response != null && response.startsWith("OK|")) {
                val playlists = parsePlaylistsFromJson(response.substringAfter("OK|"))
                runOnUiThread {
                    rvPlaylists.adapter = PlaylistSelectionAdapter(playlists) { selectedPlaylist ->
                        addSongToPlaylist(song.id, selectedPlaylist.id)
                        bottomSheet.dismiss()
                    }
                }
            }
        }
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun addSongToPlaylist(songId: Int, playlistId: Int) {
        val params = JSONObject().put("song_id", songId).put("playlist_id", playlistId)
        SocketManager.sendCommand("ADD_SONG_TO_PLAYLIST", params) { response ->
            runOnUiThread {
                if (response != null && response.startsWith("OK")) {
                    Toast.makeText(this, "Added to playlist!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseSongsFromJson(jsonString: String): List<Song> {
        val list = mutableListOf<Song>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(Song(
                    id = obj.optInt("id", 0),
                    title = obj.optString("title", "Unknown"),
                    artist = obj.optString("artist", "Unknown"),
                    coverUrl = obj.optString("cover_url", "")
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    private fun parsePlaylistsFromJson(jsonString: String): List<Playlist> {
        val list = mutableListOf<Playlist>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(Playlist(obj.getInt("id"), obj.getString("name"), obj.getInt("user_id")))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        // IMPORTANT: We do NOT release the mediaPlayer here anymore.
        // It lives in MusicPlayerManager so music continues across activities.
    }
}