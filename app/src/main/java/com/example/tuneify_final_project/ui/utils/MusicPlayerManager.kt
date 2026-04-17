package com.example.tuneify_final_project.ui.utils

import android.media.MediaPlayer

object MusicPlayerManager {
    var currentSongTitle: String? = null
    var currentArtist: String? = null
    var currentCoverUrl: String? = null
    var isPlaying: Boolean = false
    var mediaPlayer: MediaPlayer? = null
}