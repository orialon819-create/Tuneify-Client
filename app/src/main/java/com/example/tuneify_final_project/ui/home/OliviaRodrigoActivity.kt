package com.example.tuneify_final_project.ui.home

import com.example.tuneify_final_project.R

class OliviaRodrigoActivity : BaseArtistActivity() {

    override fun layoutRes()    = R.layout.activity_artist_olivia
    override fun heroImageRes() = R.drawable.add_playlist_cover  // ← swap: R.drawable.artist_olivia
    override fun artistName()   = "Olivia Rodrigo"
    override fun subLabel()     = "Modern Pop Voice"
    override fun accentColor()  = 0xFF8B5CF6.toInt()

    override fun introText() =
        "A defining voice of modern emotional pop — blending raw vulnerability with sharp, diary-like storytelling that cuts deeper than most artists twice her age."

    override fun whyTheyMatter() = listOf(
        Triple("🎤", "Signature Sound",  "Confessional pop-rock with angsty guitar riffs and arena-sized emotional hooks"),
        Triple("🌍", "Cultural Impact",  "SOUR was the most-streamed debut album by a female artist in Spotify history"),
        Triple("✍️", "Influence",        "Revived the tradition of deeply personal songwriting for an entire generation"),
        Triple("🏆", "Milestones",       "4 Grammy wins at age 20, including Best Pop Vocal Album")
    )

    override fun albums() = listOf(
        AlbumData("SOUR",  "2021", R.drawable.add_playlist_cover,
            listOf("drivers license", "good 4 u", "deja vu")),
        AlbumData("GUTS",  "2023", R.drawable.add_playlist_cover,
            listOf("vampire", "bad idea right?", "get him back!"))
    )

    override fun tracks() = listOf(
        TrackData("drivers license", "breakthrough hit",    "drivers license"),
        TrackData("good 4 u",        "fan favorite",        "good 4 u"),
        TrackData("vampire",         "defining anthem",     "vampire"),
        TrackData("deja vu",         "fan deep cut",        "deja vu")
    )

    override fun facts() = listOf(
        FactData("✏️", "She wrote 'drivers license' at 16 about a real person — and the internet found out who within days."),
        FactData("🎸", "GUTS debuted at #1 in 15 countries in its first week of release."),
        FactData("🎬", "She starred in High School Musical: The Musical: The Series before blowing up globally."),
        FactData("📱", "'drivers license' broke the record for most Spotify streams in a single week when it dropped.")
    )

    override fun footerMood() =
        "You're driving alone at night, feeling too many things at once."
}