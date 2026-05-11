package com.example.tuneify_final_project.ui.home

import com.example.tuneify_final_project.R

class PinkFloydActivity : BaseArtistActivity() {

    override fun layoutRes()    = R.layout.activity_artist_pinkfloyd
    override fun heroImageRes() = R.drawable.add_playlist_cover  // ← swap: R.drawable.artist_pinkfloyd
    override fun artistName()   = "Pink Floyd"
    override fun subLabel()     = "Architects of the Cosmic"
    override fun accentColor()  = 0xFF9333EA.toInt()

    override fun introText() =
        "Not a band — an experience. Pink Floyd didn't make songs, they built worlds. Each album is a journey you enter and don't return from quite the same person."

    override fun whyTheyMatter() = listOf(
        Triple("🌌", "Signature Sound",  "Sprawling, cinematic soundscapes with philosophical lyrics that hit hardest at 2am"),
        Triple("🎨", "Influence",        "Invented the concept album as an immersive art form — every track exists as part of a larger whole"),
        Triple("🌍", "Cultural Impact",  "The Dark Side of the Moon spent 14 years on the Billboard 200 — still the longest charting album ever"),
        Triple("🏆", "Milestones",       "Over 250 million records sold — one of the best-selling acts in history")
    )

    override fun albums() = listOf(
        AlbumData("The Dark Side of the Moon", "1973", R.drawable.add_playlist_cover,
            listOf("Money", "Time", "Us and Them")),
        AlbumData("Wish You Were Here",        "1975", R.drawable.add_playlist_cover,
            listOf("Wish You Were Here", "Shine On You Crazy Diamond")),
        AlbumData("The Wall",                  "1979", R.drawable.add_playlist_cover,
            listOf("Another Brick in the Wall", "Comfortably Numb", "Hey You"))
    )

    override fun tracks() = listOf(
        TrackData("Comfortably Numb",        "the guitar solo that ends debates",      "Comfortably Numb"),
        TrackData("Wish You Were Here",      "written for their lost bandmate Syd",    "Wish You Were Here"),
        TrackData("Money",                   "a 7/4 time signature that somehow slaps","Money"),
        TrackData("Time",                    "the alarm clocks intro that always shocks","Time")
    )

    override fun facts() = listOf(
        FactData("🏠", "Parts of The Wall were written in a hotel room where Roger Waters had actually spat on a fan."),
        FactData("🌈", "The prism on Dark Side of the Moon was chosen because it represented the band's light shows."),
        FactData("🎬", "The Wall became a full feature film in 1982, directed by Alan Parker."),
        FactData("💿", "Dark Side of the Moon was on the Billboard 200 for over 900 weeks — spanning multiple decades.")
    )

    override fun footerMood() =
        "You're lying on the floor in a dark room, letting the music fill the entire space around you."
}