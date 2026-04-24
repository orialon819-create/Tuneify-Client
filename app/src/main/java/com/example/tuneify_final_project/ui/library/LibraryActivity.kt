package com.example.tuneify_final_project.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.LibraryPlaylistAdapter
import com.example.tuneify_final_project.ui.create_playlist.CreatePlaylistActivity
import com.example.tuneify_final_project.ui.models.Playlist
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import org.json.JSONArray
import org.json.JSONObject

class LibraryActivity : AppCompatActivity() {

    private lateinit var rvPlaylists: RecyclerView
    private lateinit var tvNoPlaylists: TextView
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        PlaybackUtils.bind(this)

        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        rvPlaylists  = findViewById(R.id.rv_library_playlists)
        tvNoPlaylists = findViewById(R.id.tv_no_playlists_msg)

        // 3-column grid to match Spotify "Your Library"
        rvPlaylists.layoutManager = GridLayoutManager(this, 3)

        findViewById<ImageView>(R.id.btn_add_playlist).setOnClickListener {
            startActivity(Intent(this, CreatePlaylistActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        NavigationUtils.setupBottomNav(this)
        PlaybackUtils.updateUI(this)
        fetchPlaylists()
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackUtils.unbind(this)
    }

    private fun fetchPlaylists() {
        if (currentUserId == -1) return

        val params = JSONObject().put("user_id", currentUserId)

        SocketManager.sendCommand("GET_USER_PLAYLISTS", params) { response ->
            if (response == null || !response.startsWith("OK|")) return@sendCommand

            val playlists = parsePlaylists(response.substringAfter("OK|"))

            runOnUiThread {
                if (playlists.isEmpty()) {
                    rvPlaylists.visibility  = View.GONE
                    tvNoPlaylists.visibility = View.VISIBLE
                } else {
                    rvPlaylists.visibility  = View.VISIBLE
                    tvNoPlaylists.visibility = View.GONE

                    rvPlaylists.adapter = LibraryPlaylistAdapter(playlists) { playlist ->
                        startActivity(
                            Intent(this, PlaylistDetailsActivity::class.java).apply {
                                putExtra("PLAYLIST_ID",    playlist.id)
                                putExtra("PLAYLIST_NAME",  playlist.name)
                                putExtra("PLAYLIST_COVER", playlist.coverUrl)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun parsePlaylists(jsonString: String): List<Playlist> {
        val list = mutableListOf<Playlist>()
        try {
            val arr = JSONArray(jsonString)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Playlist(
                    o.getInt("id"),
                    o.getString("name"),
                    o.getInt("user_id"),
                    o.optString("cover_url", null)
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }
}