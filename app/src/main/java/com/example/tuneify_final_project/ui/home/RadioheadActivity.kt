package com.example.tuneify_final_project.ui.home

import com.example.tuneify_final_project.R

class RadioheadActivity : BaseArtistActivity() {

    override fun layoutRes()    = R.layout.activity_artist_radiohead
    override fun heroImageRes() = R.drawable.add_playlist_cover  // ← swap: R.drawable.artist_radiohead
    override fun artistName()   = "Radiohead"
    override fun subLabel()     = "Architects of Modern Anxiety"
    override fun accentColor()  = 0xFF2D6A9F.toInt()

    override fun introText() =
        "They didn't follow rock's path — they dismantled it. Radiohead turned paranoia, alienation, and technological dread into some of the most critically revered music ever recorded."

    override fun whyTheyMatter() = listOf(
        Triple("🧠", "Signature Sound",  "Fractured guitars, Thom Yorke's falsetto, and electronic textures that feel like a mind unravelling"),
        Triple("💿", "Influence",        "OK Computer predicted the internet age's anxiety in 1997 — years before anyone felt it"),
        Triple("🌍", "Cultural Impact",  "In Rainbows was released pay-what-you-want in 2007 — changing how musicians think about distribution"),
        Triple("🏆", "Milestones",       "Consistently top 3 in 'greatest albums of all time' polls across Rolling Stone, NME, and Pitchfork")
    )

    override fun albums() = listOf(
        AlbumData("OK Computer",  "1997", R.drawable.add_playlist_cover,
            listOf("Paranoid Android", "No Surprises", "Karma Police")),
        AlbumData("Kid A",        "2000", R.drawable.add_playlist_cover,
            listOf("Everything in Its Right Place", "How to Disappear Completely", "Idioteque")),
        AlbumData("In Rainbows",  "2007", R.drawable.add_playlist_cover,
            listOf("Weird Fishes", "Reckoner", "All I Need"))
    )

    override fun tracks() = listOf(
        TrackData("Creep",                  "the song they almost didn't release",    "Creep"),
        TrackData("Karma Police",           "OK Computer's defining moment",          "Karma Police"),
        TrackData("No Surprises",           "dystopia wrapped in a lullaby",          "No Surprises"),
        TrackData("Fake Plastic Trees",     "quietly devastating",                    "Fake Plastic Trees")
    )

    override fun facts() = listOf(
        FactData("🎸", "Radiohead almost left 'Creep' off their debut — they thought it was too embarrassing."),
        FactData("💻", "In Rainbows made more money as pay-what-you-want than their previous album through traditional sales."),
        FactData("🎬", "OK Computer was partly written in a haunted mansion they rented for recording."),
        FactData("🚫", "They refused to play Creep live for nearly a decade — then brought it back and crowds lost their minds.")
    )

    override fun footerMood() =
        "You're staring out a train window at grey skies, headphones in, world out."
}