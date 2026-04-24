package com.example.tuneify_final_project.ui.create_playlist

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.SongAdapter
import com.example.tuneify_final_project.ui.models.Song
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import com.example.tuneify_final_project.ui.adapters.SelectableSongAdapter




class CreatePlaylistActivity : AppCompatActivity() {

    private lateinit var songAdapter: SelectableSongAdapter
    private val allSearchResults = mutableListOf<Song>()
    private var currentUserId: Int = -1
    private var currentUserFirstName: String = ""

    // Image handling variables
    private lateinit var ivPlaylistCover: ImageView
    private var selectedImageUri: Uri? = null

    // --- Launchers ---
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivPlaylistCover.setImageURI(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            ivPlaylistCover.setImageBitmap(it)
            selectedImageUri = saveBitmapToCache(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)

        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)
        currentUserFirstName = sharedPref.getString("USER_FIRST_NAME", "") ?: ""

        if (currentUserId == -1) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI
        ivPlaylistCover = findViewById(R.id.iv_playlist_cover)
        val btnSelectCover = findViewById<Button>(R.id.btn_select_cover)
        val rvSongs = findViewById<RecyclerView>(R.id.rv_songs_list)
        val btnSave = findViewById<Button>(R.id.btn_save_playlist)
        val etSearch = findViewById<EditText>(R.id.et_search_songs)

        btnSelectCover.setOnClickListener { showImagePickerOptions() }

        songAdapter = SelectableSongAdapter(allSearchResults)
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter = songAdapter

        btnSave.setOnClickListener { handleSavePlaylist() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) sendSearchToServer(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        // 1. Setup the Navigation clicks
        NavigationUtils.setupBottomNav(this)
        // 2. Check if the music bar should be visible
        PlaybackUtils.updateUI(this)
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Add Playlist Cover")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            takePictureLauncher.launch()
                        } else {
                            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
                        }
                    }
                    1 -> pickImageLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun handleSavePlaylist() {
        val etPlaylistName = findViewById<EditText>(R.id.et_playlist_name)
        val playlistName = etPlaylistName.text.toString().trim()
        val selectedIds = allSearchResults.filter { it.isSelected }.map { it.id }

        if (playlistName.isEmpty() || selectedIds.isEmpty()) {
            Toast.makeText(this, "Fill name and select songs", Toast.LENGTH_SHORT).show()
            return
        }

        val params = JSONObject().apply {
            put("user_id", currentUserId)
            put("playlist_name", playlistName)
            put("songs", JSONArray(selectedIds))
        }

        // Step 1: Create Playlist via Socket to get ID
        SocketManager.sendCommand("CREATE_PLAYLIST", params) { response ->
            if (response != null && response.startsWith("OK|")) {
                val newPlaylistId = response.substringAfter("OK|").trim().toIntOrNull() ?: -1

                // Step 2: Upload Image if selected
                if (newPlaylistId != -1 && selectedImageUri != null) {
                    uploadImageToServer(newPlaylistId, selectedImageUri!!)
                } else {
                    runOnUiThread { finish() }
                }
            }
        }
    }

    private fun uploadImageToServer(playlistId: Int, uri: Uri) {
        val file = getFileFromUri(uri) ?: return

        // 1. Specify image/jpeg specifically
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        // 2. Ensure port 8000 is used (FastAPI)
        val request = Request.Builder()
            .url("http://${NetworkConfig.serverIp}:8000/upload_playlist_cover/$playlistId")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@CreatePlaylistActivity, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val filename = json.getString("filename")

                    // Step 3: Tell Socket Server the filename (Socket uses its own port, usually 5005)
                    val updateParams = JSONObject().apply {
                        put("playlist_id", playlistId)
                        put("filename", filename)
                    }
                    SocketManager.sendCommand("UPDATE_PLAYLIST_COVER", updateParams) {
                        runOnUiThread {
                            Toast.makeText(this@CreatePlaylistActivity, "Playlist Created!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        })
    }

    // --- HELPER METHODS ---

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "temp_cover_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(file)
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(cacheDir, "upload_image.jpg")
        inputStream.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun sendSearchToServer(query: String) {
        val params = JSONObject().put("query", query)
        SocketManager.sendCommand("SEARCH_SONGS", params) { response ->
            if (response != null && response.startsWith("OK|")) {
                val rawJson = response.substringAfter("OK|")
                val songs = parseSongsFromJson(rawJson)
                runOnUiThread {
                    allSearchResults.clear()
                    allSearchResults.addAll(songs)
                    songAdapter.updateList(allSearchResults)
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
                    id = obj.getInt("id"),
                    title = obj.getString("title"),
                    artist = obj.getString("artist"),
                    coverUrl = obj.optString("cover_url", null) // Add this!
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }
}