package com.example.tuneify_final_project.ui.home

/**
 * Holds all content for a single artist page.
 * Pass this via intent to the correct ArtistDetailActivity.
 */
data class AlbumData(
    val title: String,
    val year: String,
    val coverRes: Int,
    val trackHighlights: List<String>
)

data class TrackData(
    val title: String,
    val tag: String,
    val streamTitle: String
)

data class FactData(
    val emoji: String,
    val fact: String
)