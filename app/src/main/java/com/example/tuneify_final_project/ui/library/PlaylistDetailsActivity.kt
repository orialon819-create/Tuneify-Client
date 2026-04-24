package com.example.tuneify_final_project.ui.library

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.SongAdapter
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import org.json.JSONArray
import org.json.JSONObject

class PlaylistDetailsActivity : AppCompatActivity() {

    private val songsList = mutableListOf<Song>()
    private lateinit var btnPlay: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_details)

        // Register listener once keyed by class name — no duplicates on rotation
        PlaybackUtils.bind(this)

        // Separate listener just for the play button icon in this screen
        MusicPlayerManager.addListener("PlaylistDetailsPlay") {
            runOnUiThread { syncPlayButton() }
        }

        val name  = intent.getStringExtra("PLAYLIST_NAME")
        val cover = intent.getStringExtra("PLAYLIST_COVER")
        val id    = intent.getIntExtra("PLAYLIST_ID", -1)

        findViewById<TextView>(R.id.tv_detail_title).text = name
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        Glide.with(this)
            .load("http://${NetworkConfig.serverIp}:8000/covers/playlist/$cover")
            .placeholder(R.drawable.add_playlist_cover)
            .error(R.drawable.add_playlist_cover)
            .into(findViewById(R.id.iv_detail_cover))

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_playlist_songs)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = SongAdapter(songsList) { song ->
            PlaybackUtils.playSong(this, song, songsList)
            PlaybackUtils.updateUI(this) // 🔥 FIX
        }
        rv.adapter = adapter

        btnPlay = findViewById(R.id.btn_play_playlist)
        btnPlay.setOnClickListener {
            if (songsList.isEmpty()) return@setOnClickListener
            // Toggle if this playlist is already loaded, otherwise start fresh
            if (MusicPlayerManager.currentPlaylist === songsList) {
                MusicPlayerManager.togglePlayPause()
            } else {
                MusicPlayerManager.playPlaylist(songsList, 0)
            }
            // listener above will call syncPlayButton() automatically
        }

        fetchSongs(id, adapter)
    }

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

    private fun fetchSongs(id: Int, adapter: SongAdapter) {
        val params = JSONObject().put("playlist_id", id)
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
                    o.getInt("id"),
                    o.getString("title"),
                    o.getString("artist"),
                    o.optString("cover_url", "")
                ))
            }
            runOnUiThread {
                songsList.clear()
                songsList.addAll(list)
                adapter.notifyDataSetChanged()
                syncPlayButton()
            }
        }
    }
}