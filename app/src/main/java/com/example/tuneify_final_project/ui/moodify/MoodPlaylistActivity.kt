package com.example.tuneify_final_project.ui.moodify

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.SongAdapter
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 * SCREEN 5 — Mood Playlist.
 * Fetches 5 random songs by mood and shows them as a playable list.
 */
class MoodPlaylistActivity : AppCompatActivity() {

    private val moodEmoji = mapOf(
        "Happy" to "😊", "Sad" to "😢",
        "Angry" to "😠", "Calm" to "😌", "Energetic" to "⚡"
    )

    private val songs = mutableListOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_playlist)

        PlaybackUtils.bind(this)

        val mood  = intent.getStringExtra("MOOD") ?: "Happy"
        val emoji = moodEmoji[mood] ?: "🎵"

        findViewById<ImageView>(R.id.btn_playlist_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_playlist_title).text = "Your $mood Playlist"
        findViewById<TextView>(R.id.tv_playlist_emoji).text = emoji

        val rv = findViewById<RecyclerView>(R.id.rv_mood_songs)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = SongAdapter(songs, onSongClick = { song ->
            PlaybackUtils.playSong(this, song, songs)
        })
        rv.adapter = adapter

        findViewById<Button>(R.id.btn_shuffle).setOnClickListener {
            if (songs.isNotEmpty()) {
                MusicPlayerManager.playPlaylist(songs.shuffled(), 0)
            }
        }

        fetchMoodSongs(mood, adapter)
    }

    private fun fetchMoodSongs(mood: String, adapter: SongAdapter) {
        val params = JSONObject().put("mood", mood).put("count", 5)

        SocketManager.sendCommand("GET_SONGS_BY_MOOD_LIST", params) { response ->
            if (response?.startsWith("OK|") != true) return@sendCommand

            val arr  = JSONArray(response.substringAfter("OK|"))
            val list = mutableListOf<Song>()

            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Song(
                    id       = o.getInt("id"),
                    title    = o.getString("title"),
                    artist   = o.getString("artist"),
                    coverUrl = o.optString("cover_url", "")
                ))
            }

            runOnUiThread {
                songs.clear()
                songs.addAll(list)
                adapter.updateList(songs.toList())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PlaybackUtils.updateUI(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackUtils.unbind(this)
    }
}