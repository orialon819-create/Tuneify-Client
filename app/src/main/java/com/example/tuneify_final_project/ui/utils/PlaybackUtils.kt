package com.example.tuneify_final_project.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.FullPlayerActivity

object PlaybackUtils {

    fun bind(activity: Activity) {
        val key = activity.javaClass.simpleName
        MusicPlayerManager.addListener(key) {
            activity.runOnUiThread { updateUI(activity) }
        }
    }

    fun unbind(activity: Activity) {
        MusicPlayerManager.removeListener(activity.javaClass.simpleName)
    }

    fun playSong(context: Context, song: Song, playlist: List<Song>) {
        val index = playlist.indexOfFirst { it.id == song.id }
        MusicPlayerManager.playPlaylist(playlist, if (index >= 0) index else 0)
    }

    fun updateUI(activity: Activity) {
        // Check both possible include IDs used across different layouts
        val bar: View = activity.findViewById(R.id.cv_now_playing_bar)
            ?: activity.findViewById(R.id.cv_now_playing_bar)
            ?: return

        val title = MusicPlayerManager.currentSongTitle
        if (title == null) {
            bar.visibility = View.GONE
            return
        }

        bar.visibility = View.VISIBLE

        activity.findViewById<TextView>(R.id.tv_now_playing_title)?.text = title
        activity.findViewById<TextView>(R.id.tv_now_playing_artist)?.text =
            MusicPlayerManager.currentArtist

        // Load album art into the bar thumbnail
        val thumb = activity.findViewById<ImageView>(R.id.iv_now_playing_thumb)
        if (thumb != null) {
            val coverUrl = MusicPlayerManager.currentCoverUrl
            if (!coverUrl.isNullOrEmpty() && coverUrl != "null") {
                Glide.with(activity)
                    .load("http://${NetworkConfig.serverIp}:8000/covers/song/$coverUrl")
                    .placeholder(R.drawable.add_playlist_cover)
                    .error(R.drawable.add_playlist_cover)
                    .into(thumb)
            } else {
                thumb.setImageResource(R.drawable.add_playlist_cover)
            }
        }

        bar.setOnClickListener {
            val intent = Intent(activity, FullPlayerActivity::class.java)
            activity.startActivity(intent)
        }

        // Always sync the play/pause icon to actual state
        val btnMini = activity.findViewById<ImageView>(R.id.iv_now_playing_play_pause)
        btnMini?.setImageResource(
            if (MusicPlayerManager.isPlaying) R.drawable.pause_icon else R.drawable.play_icon
        )
        btnMini?.setOnClickListener { MusicPlayerManager.togglePlayPause() }
    }


}