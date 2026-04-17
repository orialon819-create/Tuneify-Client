package com.example.tuneify_final_project.ui.models

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val coverUrl: String? = null,
    var isSelected: Boolean = false
)