package com.example.tuneify_final_project.ui.home

import com.example.tuneify_final_project.R

class MazzyStarActivity : BaseArtistActivity() {

    override fun layoutRes()    = R.layout.activity_artist_mazzystar
    override fun heroImageRes() = R.drawable.mazzystar_cover  // ← swap: R.drawable.artist_mazzystar
    override fun artistName()   = "Mazzy Star"
    override fun subLabel()     = "Dream Pop Mystics"
    override fun accentColor()  = 0xFF7B6FA0.toInt()

    override fun introText() =
        "Music that sounds like a half-remembered dream. Hope Sandoval's voice doesn't perform — it drifts. Mazzy Star made melancholy feel like the most beautiful place to be."

    override fun whyTheyMatter() = listOf(
        Triple("🌫️", "Signature Sound",  "Hazy, reverb-soaked dream pop with slide guitar and vocals that feel like smoke in still air"),
        Triple("✨", "Influence",        "Defined the aesthetic of 90s dream pop and still appears in films, shows, and playlists decades later"),
        Triple("🎬", "Cultural Impact",  "'Fade Into You' became a cultural shorthand for romantic longing — used in everything from TV shows to films"),
        Triple("🌙", "Milestones",       "Despite limited releases, their catalogue has accumulated hundreds of millions of streams in the streaming era")
    )

    override fun albums() = listOf(
        AlbumData("So Tonight That I Might See", "1993", R.drawable.sotonight_cover,
            listOf("Fade Into You", "Bells Ring", "Mary of Silence")),
        AlbumData("Among My Swan",               "1996", R.drawable.amongmyswan_cover,
            listOf("Flowers in December", "Disappear", "Into Dust")),
        AlbumData("She Hangs Brightly",          "1990", R.drawable.shehangs_cover,
            listOf("Halah", "Blue Flower", "Give You My Lovin'"))
    )

    override fun tracks() = listOf(
        TrackData("Fade Into You",       "the song that never gets old",           "Fade Into You"),
        TrackData("Into Dust",           "hauntingly quiet and devastating",       "Into Dust"),
        TrackData("Flowers in December","melancholy at its most beautiful",        "Flowers in December"),
        TrackData("Blue Flower",         "dream pop perfection",                   "Blue Flower")
    )

    override fun facts() = listOf(
        FactData("🎬", "'Fade Into You' has appeared in over 50 TV shows and films — including My So-Called Life and Euphoria."),
        FactData("🔇", "Hope Sandoval is famously reclusive — she rarely gives interviews and almost never tours."),
        FactData("⏳", "The band went on a 17-year hiatus between 1997 and 2011, then returned quietly with new music."),
        FactData("💫", "'Fade Into You' was written in 10 minutes according to David Roback. It became their most iconic song.")
    )

    override fun footerMood() =
        "You're watching golden hour fade through dusty curtains, completely alone and completely okay with it."
}