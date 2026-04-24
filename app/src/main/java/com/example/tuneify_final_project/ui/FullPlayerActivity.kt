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

class FullPlayerActivity : AppCompatActivity() {

    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnPlay: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnPrev: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var root: View

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_player)

        ivCover = findViewById(R.id.iv_full_cover)
        tvTitle = findViewById(R.id.tv_full_title)
        tvArtist = findViewById(R.id.tv_full_artist)
        btnPlay = findViewById(R.id.btn_full_play)
        btnNext = findViewById(R.id.btn_next)
        btnPrev = findViewById(R.id.btn_prev)
        seekBar = findViewById(R.id.seek_bar)
        root = findViewById(R.id.player_container)

        // 🎧 Play / Pause
        btnPlay.setOnClickListener {
            MusicPlayerManager.togglePlayPause()
            updateUI()
        }

        // ⏭ Next
        btnNext.setOnClickListener {
            MusicPlayerManager.playPlaylist(
                MusicPlayerManager.currentPlaylist,
                MusicPlayerManager.currentIndex + 1
            )
        }

        // ⏮ Previous
        btnPrev.setOnClickListener {
            val prev = (MusicPlayerManager.currentIndex - 1).coerceAtLeast(0)
            MusicPlayerManager.playPlaylist(
                MusicPlayerManager.currentPlaylist,
                prev
            )
        }

        // 🔥 Listen to player changes (VERY IMPORTANT)
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

    // 🎨 Update UI
    private fun updateUI() {
        tvTitle.text = MusicPlayerManager.currentSongTitle ?: ""
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
    }

    // ⏱ Seekbar updater
    private fun startSeekBarUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                val player = MusicPlayerManager.getPlayer()

                if (player != null) {
                    seekBar.max = player.duration
                    seekBar.progress = player.currentPosition
                }

                handler.postDelayed(this, 500)
            }
        })

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicPlayerManager.getPlayer()?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    // 👇 Swipe down to close (Spotify style)
    private fun setupSwipeToClose() {
        var startY = 0f

        root.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val diff = event.rawY - startY

                    if (diff > 0) {
                        root.translationY = diff
                        root.alpha = 1 - (diff / 1000)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val diff = event.rawY - startY

                    if (diff > 250) {
                        finish() // 🔥 close screen
                    } else {
                        root.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                }
            }
            true
        }
    }
}