package com.example.tuneify_final_project.ui.search

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
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.PlaylistSelectionAdapter
import com.example.tuneify_final_project.ui.adapters.SearchSongAdapter
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private lateinit var rvResults: RecyclerView
    private lateinit var searchAdapter: SearchSongAdapter
    private lateinit var etSearch: EditText
    private val searchResults = mutableListOf<Song>()
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        PlaybackUtils.bind(this)

        NavigationUtils.setupBottomNav(this)
        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        rvResults = findViewById(R.id.rv_search_results)
        etSearch = findViewById(R.id.et_search_input)

        searchAdapter = SearchSongAdapter(
            searchResults,
            onSongClick = { song -> PlaybackUtils.playSong(this, song, searchResults) },
            onMoreClick = { song -> openPlaylistMenu(song) }
        )
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = searchAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) performSearch(query)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        PlaybackUtils.updateUI(this)
    }

    private fun performSearch(query: String) {
        val params = JSONObject().put("query", query)
        SocketManager.sendCommand("SEARCH_SONGS", params) { response ->
            if (response != null && response.startsWith("OK|")) {
                val songs = parseSongsFromJson(response.substringAfter("OK|"))
                runOnUiThread {
                    searchResults.clear()
                    searchResults.addAll(songs)
                    searchAdapter.updateList(songs)
                }
            }
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
                val playlists = JSONArray(response.substringAfter("OK|")) // simplified parse
                // Add adapter logic here
            }
        }
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun parseSongsFromJson(jsonString: String): List<Song> {
        val list = mutableListOf<Song>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(Song(obj.getInt("id"), obj.getString("title"), obj.getString("artist"), obj.optString("cover_url", "")))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }
}