package com.example.tuneify_final_project.ui.models

data class Artist(
    val name: String,
    val genre: String,
    val bio: String,
    val monthlyListeners: String,
    val photoResId: Int,
    val songs: List<Song>
)