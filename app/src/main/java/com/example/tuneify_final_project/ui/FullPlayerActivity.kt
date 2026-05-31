package com.example.tuneify_final_project.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import org.json.JSONObject

/**
 * FullPlayerActivity is the full-screen music player UI.
 * It provides:
 * - Full song playback controls (play/pause/next/previous)
 * - Like functionality using backend playlists
 * - Live seek bar updates with time tracking
 * - Swipe-to-close gesture
 * - Synchronization with MusicPlayerManager state
 */
class FullPlayerActivity : AppCompatActivity() {

    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnPlay: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnPrev: ImageView
    private lateinit var btnLike: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var tvElapsed: TextView
    private lateinit var tvDuration: TextView
    private lateinit var root: View

    private val handler = Handler(Looper.getMainLooper())

    private var isLiked = false
    private var currentUserId = -1

    // Input: savedInstanceState (Bundle?)
    // Output: none
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_player)

        ivCover    = findViewById(R.id.iv_full_cover)
        tvTitle    = findViewById(R.id.tv_full_title)
        tvArtist   = findViewById(R.id.tv_full_artist)
        btnPlay    = findViewById(R.id.btn_full_play)
        btnNext    = findViewById(R.id.btn_next)
        btnPrev    = findViewById(R.id.btn_prev)
        btnLike    = findViewById(R.id.btn_like)
        seekBar    = findViewById(R.id.seek_bar)
        tvElapsed  = findViewById(R.id.tv_elapsed)
        tvDuration = findViewById(R.id.tv_duration)
        root       = findViewById(R.id.player_container)

        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        btnPlay.setOnClickListener {
            MusicPlayerManager.togglePlayPause()
            updateUI()
        }

        btnNext.setOnClickListener {
            MusicPlayerManager.playPlaylist(
                MusicPlayerManager.currentPlaylist,
                MusicPlayerManager.currentIndex + 1
            )
        }

        btnPrev.setOnClickListener {
            val prev = (MusicPlayerManager.currentIndex - 1).coerceAtLeast(0)
            MusicPlayerManager.playPlaylist(MusicPlayerManager.currentPlaylist, prev)
        }

        btnLike.setOnClickListener { toggleLike() }

        MusicPlayerManager.addListener("FullPlayer") {
            runOnUiThread { updateUI() }
        }

        setupSwipeToClose()
        updateUI()
        startSeekBarUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerManager.removeListener("FullPlayer")
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateUI() {

        tvTitle.text  = MusicPlayerManager.currentSongTitle ?: ""
        tvArtist.text = MusicPlayerManager.currentArtist ?: ""

        val cover = MusicPlayerManager.currentCoverUrl
        if (!cover.isNullOrEmpty() && cover != "null") {
            Glide.with(this)
                .load("http://${NetworkConfig.serverIp}:8000/covers/song/$cover")
                .placeholder(R.drawable.add_playlist_cover)
                .error(R.drawable.add_playlist_cover)
                .into(ivCover)
        } else {
            ivCover.setImageResource(R.drawable.add_playlist_cover)
        }

        btnPlay.setImageResource(
            if (MusicPlayerManager.isPlaying)
                R.drawable.pause_icon
            else
                R.drawable.play_icon
        )

        isLiked = false
        updateLikeIcon()

        val songId = MusicPlayerManager.currentPlaylist
            .getOrNull(MusicPlayerManager.currentIndex)?.id

        if (songId != null && currentUserId != -1) {

            val p1 = JSONObject().put("user_id", currentUserId)

            SocketManager.sendCommand("GET_OR_CREATE_LIKED_SONGS", p1) { response ->
                if (response?.startsWith("OK|") != true) return@sendCommand

                val likedPlaylistId =
                    response.substringAfter("OK|").trim().toIntOrNull() ?: return@sendCommand

                checkIfLiked(songId, likedPlaylistId)
            }
        }
    }
    // Input: songId (Int), likedPlaylistId (Int)
    // Output: none
    private fun checkIfLiked(songId: Int, likedPlaylistId: Int) {

        val p = JSONObject()
            .put("playlist_id", likedPlaylistId)
            .put("song_id", songId)

        SocketManager.sendCommand("CHECK_SONG_IN_PLAYLIST", p) { response ->
            if (response?.startsWith("OK|") == true) {
                isLiked = response.substringAfter("OK|").toBoolean()
                runOnUiThread { updateLikeIcon() }
            }
        }
    }

    // LIKE TOGGLE
    private fun toggleLike() {
        if (currentUserId == -1) return

        val songId = MusicPlayerManager.currentPlaylist
            .getOrNull(MusicPlayerManager.currentIndex)?.id ?: return

        val p1 = JSONObject().put("user_id", currentUserId)

        SocketManager.sendCommand("GET_OR_CREATE_LIKED_SONGS", p1) { response ->
            if (response?.startsWith("OK|") != true) return@sendCommand

            val likedPlaylistId =
                response.substringAfter("OK|").trim().toIntOrNull() ?: return@sendCommand

            if (!isLiked) {
                val p2 = JSONObject()
                    .put("playlist_id", likedPlaylistId)
                    .put("song_id", songId)

                SocketManager.sendCommand("ADD_SONG_TO_PLAYLIST", p2) { res ->
                    if (res?.startsWith("OK") == true) {
                        isLiked = true
                        runOnUiThread { updateLikeIcon() }
                    }
                }
            } else {
                val p2 = JSONObject()
                    .put("playlist_id", likedPlaylistId)
                    .put("song_id", songId)

                SocketManager.sendCommand("REMOVE_SONG_FROM_PLAYLIST", p2) { res ->
                    if (res?.startsWith("OK") == true) {
                        isLiked = false
                        runOnUiThread { updateLikeIcon() }
                    }
                }
            }
        }
    }

    private fun updateLikeIcon() {
        btnLike.setImageResource(
            if (isLiked)
                R.drawable.ic_heart_filled
            else
                R.drawable.ic_heart_outline
        )
    }

    // SEEK BAR
    private fun startSeekBarUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                val player = MusicPlayerManager.getPlayer()
                if (player != null) {
                    seekBar.max = player.duration
                    seekBar.progress = player.currentPosition
                    tvElapsed.text = formatMs(player.currentPosition)
                    tvDuration.text = formatMs(player.duration)
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun formatMs(ms: Int): String {
        val sec = ms / 1000
        return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
    }

    // SWIPE CLOSE
    private fun setupSwipeToClose() {
        var startY = 0f

        root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startY = event.rawY

                MotionEvent.ACTION_MOVE -> {
                    val diff = event.rawY - startY
                    if (diff > 0) {
                        root.translationY = diff
                        root.alpha = 1 - (diff / 1000)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val diff = event.rawY - startY
                    if (diff > 250) finish()
                    else root.animate().translationY(0f).alpha(1f).setDuration(200).start()
                }
            }
            true
        }
    }
}