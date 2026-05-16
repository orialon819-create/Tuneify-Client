package com.example.tuneify_final_project.ui.moodify

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import org.json.JSONObject

/**
 * SCREEN 4 — Single Song Player for Moodify.
 * Fetches one random song by mood and plays it immediately.
 */
class MoodSingleSongActivity : AppCompatActivity() {

    private val moodEmoji = mapOf(
        "Happy" to "😊", "Sad" to "😢",
        "Angry" to "😠", "Calm" to "😌", "Energetic" to "⚡"
    )

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var seekBar: SeekBar
    private lateinit var tvElapsed: TextView
    private lateinit var tvDuration: TextView
    private lateinit var btnPlay: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_single_song)

        PlaybackUtils.bind(this)

        val mood  = intent.getStringExtra("MOOD") ?: "Happy"
        val emoji = moodEmoji[mood] ?: "🎵"

        seekBar    = findViewById(R.id.seek_bar_mood)
        tvElapsed  = findViewById(R.id.tv_mood_elapsed)
        tvDuration = findViewById(R.id.tv_mood_duration)
        btnPlay    = findViewById(R.id.btn_mood_play)

        findViewById<ImageView>(R.id.btn_mood_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_mood_tag).text = "Mood: $mood $emoji"

        btnPlay.setOnClickListener { MusicPlayerManager.togglePlayPause() }

        MusicPlayerManager.addListener("MoodSingleSong") {
            runOnUiThread { updatePlayButton() }
        }

        fetchAndPlay(mood)
        startSeekBarUpdates()
    }

    private fun fetchAndPlay(mood: String) {
        val params = JSONObject().put("mood", mood).put("count", 1)
        SocketManager.sendCommand("GET_SONGS_BY_MOOD_LIST", params) { response ->
            if (response?.startsWith("OK|") != true) return@sendCommand
            val arr  = org.json.JSONArray(response.substringAfter("OK|"))
            if (arr.length() == 0) return@sendCommand
            val o    = arr.getJSONObject(0)
            val song = Song(
                id       = o.getInt("id"),
                title    = o.getString("title"),
                artist   = o.getString("artist"),
                coverUrl = o.optString("cover_url", "")
            )
            runOnUiThread {
                findViewById<TextView>(R.id.tv_mood_song_title).text  = song.title
                findViewById<TextView>(R.id.tv_mood_song_artist).text = song.artist

                val coverUrl = song.coverUrl
                if (!coverUrl.isNullOrEmpty() && coverUrl != "null") {
                    Glide.with(this)
                        .load("http://${NetworkConfig.serverIp}:8000/covers/song/$coverUrl")
                        .placeholder(R.drawable.add_playlist_cover)
                        .into(findViewById(R.id.iv_mood_cover))
                }

                MusicPlayerManager.playPlaylist(listOf(song), 0)
            }
        }
    }

    private fun updatePlayButton() {
        btnPlay.setImageResource(
            if (MusicPlayerManager.isPlaying) R.drawable.pause_icon else R.drawable.play_icon
        )
    }

    private fun startSeekBarUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                MusicPlayerManager.getPlayer()?.let { player ->
                    seekBar.max      = player.duration
                    seekBar.progress = player.currentPosition
                    tvElapsed.text   = formatMs(player.currentPosition)
                    tvDuration.text  = formatMs(player.duration)
                }
                handler.postDelayed(this, 500)
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) MusicPlayerManager.getPlayer()?.seekTo(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun formatMs(ms: Int): String {
        if (ms <= 0) return "0:00"
        val sec = ms / 1000
        return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
    }

    override fun onResume() {
        super.onResume()
        PlaybackUtils.updateUI(this)
        updatePlayButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        MusicPlayerManager.removeListener("MoodSingleSong")
        PlaybackUtils.unbind(this)
    }
}