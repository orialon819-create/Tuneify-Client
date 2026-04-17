package com.example.tuneify_final_project.ui.utils

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig

object PlaybackUtils {
    fun updateUI(activity: Activity) {
        val bar = activity.findViewById<View>(R.id.now_playing_bar_include)
            ?: activity.findViewById<View>(R.id.cv_now_playing_bar)
            ?: return

        // 1. ALWAYS show the bar if we have at least once selected a song
        // If you want it visible even on first boot, remove this 'if'
        if (MusicPlayerManager.currentSongTitle != null) {
            bar.visibility = View.VISIBLE

            activity.findViewById<TextView>(R.id.tv_now_playing_title)?.text = MusicPlayerManager.currentSongTitle
            activity.findViewById<TextView>(R.id.tv_now_playing_artist)?.text = MusicPlayerManager.currentArtist

            val ivCover = activity.findViewById<ImageView>(R.id.iv_now_playing_thumb)
            val fileName = MusicPlayerManager.currentCoverUrl ?: ""
            val imageUrl = "http://${NetworkConfig.serverIp}:8000/covers/song/$fileName"

            Glide.with(activity)
                .load(imageUrl)
                .placeholder(R.drawable.add_playlist_cover)
                .centerCrop()
                .into(ivCover ?: return)

            val playPauseBtn = activity.findViewById<ImageView>(R.id.iv_now_playing_play_pause)
            playPauseBtn?.setImageResource(if (MusicPlayerManager.isPlaying) R.drawable.pause_icon else R.drawable.play_icon)

            playPauseBtn?.setOnClickListener {
                val mp = MusicPlayerManager.mediaPlayer
                if (MusicPlayerManager.isPlaying) {
                    mp?.pause()
                    MusicPlayerManager.isPlaying = false
                } else {
                    // Safety check: if mediaPlayer was released, you might need to re-prepare
                    mp?.start()
                    MusicPlayerManager.isPlaying = true
                }
                updateUI(activity)
            }
        } else {
            // OPTIONAL: Keep it hidden until the VERY FIRST song is played
            bar.visibility = View.GONE
        }
    }
}