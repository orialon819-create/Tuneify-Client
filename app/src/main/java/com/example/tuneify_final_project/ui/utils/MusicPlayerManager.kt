package com.example.tuneify_final_project.ui.utils

import android.media.MediaPlayer
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.models.Song

/**
 * MusicPlayerManager is a singleton responsible for handling audio playback in the app.
 *
 * It manages:
 * - Current song/playlist state
 * - MediaPlayer lifecycle
 * - Play/pause control
 * - Auto-play next song in playlist
 * - UI update listeners across multiple screens
 */
object MusicPlayerManager {

    var currentSongTitle: String? = null
    var currentArtist: String? = null
    var currentCoverUrl: String? = null
    var currentPlaylist: List<Song> = emptyList()
    var currentIndex: Int = 0

    private var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }

    // Keyed listeners — each screen registers once by its class name
    private val listeners = mutableMapOf<String, () -> Unit>()

    // Input: key (String), listener (() -> Unit)
    // Output: none
    fun addListener(key: String, listener: () -> Unit) {
        listeners[key] = listener
    }

    // Input: key (String)
    // Output: none
    fun removeListener(key: String) {
        listeners.remove(key)
    }

    // Input: none
    // Output: none
    private fun notifyChange() {
        listeners.values.toList().forEach { it.invoke() }
    }

    // Playback
    // Input: songs (List<Song>), startIndex (Int)
    // Output: none
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        currentPlaylist = songs
        playFromIndex(startIndex)
    }

    // Input: none
    // Output: MediaPlayer?
    fun getPlayer(): MediaPlayer? = mediaPlayer

    // Input: index (Int)
    // Output: none
    private fun playFromIndex(index: Int) {
        if (index >= currentPlaylist.size) {
            // Playlist finished — clear everything
            releasePlayer()
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

        val url = "http://${NetworkConfig.serverIp}:8000/stream/" +
                song.title.replace(" ", "%20")

        releasePlayer()

        val mp = MediaPlayer()
        mediaPlayer = mp

        mp.setDataSource(url)
        mp.prepareAsync()

        mp.setOnPreparedListener { player ->
            player.start()
            notifyChange()
        }

        mp.setOnCompletionListener {
            playFromIndex(index + 1)
        }

        mp.setOnErrorListener { _, _, _ ->
            notifyChange()
            true
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) mp.pause() else mp.start()
        notifyChange()
    }

    private fun releasePlayer() {
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
    }
}