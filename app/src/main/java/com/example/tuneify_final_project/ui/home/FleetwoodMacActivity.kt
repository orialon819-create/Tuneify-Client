package com.example.tuneify_final_project.ui.home

import com.example.tuneify_final_project.R

class FleetwoodMacActivity : BaseArtistActivity() {

    override fun layoutRes()    = R.layout.activity_artist_fleetwood
    override fun heroImageRes() = R.drawable.fleetwoodmac_cover  // ← swap: R.drawable.artist_fleetwood
    override fun artistName()   = "Fleetwood Mac"
    override fun subLabel()     = "Timeless Rock Icons"
    override fun accentColor()  = 0xFFB8902A.toInt()

    override fun introText() =
        "A band that lived every lyric they sang. Broken hearts, tangled romances, and musical genius colliding into some of rock's most enduring songs — recorded while it was all falling apart."

    override fun whyTheyMatter() = listOf(
        Triple("🎵", "Signature Sound",  "Lush harmonies and soft-rock poetry — melodies that feel like memories you never actually had"),
        Triple("💔", "Influence",        "Rumours was written while two couples in the band were simultaneously breaking up mid-recording"),
        Triple("🌍", "Cultural Impact",  "Dreams went viral on TikTok in 2020 — 43 years after its release"),
        Triple("🏆", "Milestones",       "Rumours spent 31 weeks at #1 and remains one of the best-selling albums of all time")
    )

    override fun albums() = listOf(
        AlbumData("Rumours",           "1977", R.drawable.rumors_cover,
            listOf("Go Your Own Way", "Dreams", "The Chain")),
        AlbumData("Tango in the Night","1987", R.drawable.tangointhenight,
            listOf("Big Love", "Little Lies", "Everywhere")),
        AlbumData("Fleetwood Mac",     "1975", R.drawable.fleetwoodmacalbum_cover,
            listOf("Rhiannon", "Landslide", "Say You Love Me"))
    )

    override fun tracks() = listOf(
        TrackData("Go Your Own Way", "written about a real breakup mid-tour", "Go Your Own Way"),
        TrackData("The Chain",       "the only song all 5 members co-wrote",  "The Chain"),
        TrackData("Dreams",          "went viral 43 years after release",     "Dreams"),
        TrackData("Landslide",       "written at 26, still feels timeless",   "Landslide")
    )

    override fun facts() = listOf(
        FactData("🏠", "Rumours was recorded while two couples in the band were actively breaking up with each other."),
        FactData("🛹", "A man went viral skateboarding to 'Dreams' — Stevie Nicks personally sent him a gift."),
        FactData("🎸", "The band has had over 30 members since forming in 1967."),
        FactData("🌙", "Stevie Nicks is in the Rock and Roll Hall of Fame twice — once solo, once with the band.")
    )

    override fun footerMood() =
        "You're sitting by a window in the rain, wondering about the one that got away."
}