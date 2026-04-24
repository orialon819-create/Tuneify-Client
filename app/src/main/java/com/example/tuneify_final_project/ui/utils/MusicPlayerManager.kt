package com.example.tuneify_final_project.ui.utils

import android.media.MediaPlayer
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.models.Song

object MusicPlayerManager {

    var currentSongTitle: String? = null
    var currentArtist: String? = null
    var currentCoverUrl: String? = null
    var currentPlaylist: List<Song> = emptyList()
    var currentIndex: Int = 0

    private var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    private val listeners = mutableMapOf<String, () -> Unit>()

    fun addListener(key: String, listener: () -> Unit) {
        listeners[key] = listener
    }

    fun removeListener(key: String) {
        listeners.remove(key)
    }

    private fun notifyChange() {
        listeners.values.forEach { it.invoke() }
    }

    // ───────────────────────────────────────────────

    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        currentPlaylist = songs
        playFromIndex(startIndex)
    }

    private fun playFromIndex(index: Int) {
        if (index >= currentPlaylist.size) {
            release()
            currentSongTitle = null
            currentArtist = null
            currentCoverUrl = null
            notifyChange()
            return
        }

        currentIndex = index
        val song = currentPlaylist[index]

        currentSongTitle = song.title
        currentArtist = song.artist
        currentCoverUrl = song.coverUrl

        notifyChange() // 🔥 important: update UI immediately

        val url = "http://${NetworkConfig.serverIp}:8000/stream/${song.id}"

        release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)

            setOnPreparedListener {
                it.start()
                notifyChange()
            }

            setOnCompletionListener {
                playFromIndex(index + 1)
            }

            setOnErrorListener { _, _, _ ->
                notifyChange()
                true
            }

            prepareAsync()
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause() else it.start()
            notifyChange()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}