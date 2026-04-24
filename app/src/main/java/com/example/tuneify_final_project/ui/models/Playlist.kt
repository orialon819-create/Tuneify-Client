package com.example.tuneify_final_project.ui.models

data class Playlist(
    val id: Int,
    val name: String,
    val userId: Int,
    val coverUrl: String? = null // Add this field
)