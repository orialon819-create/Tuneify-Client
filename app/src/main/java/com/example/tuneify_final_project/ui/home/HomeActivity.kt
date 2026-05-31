package com.example.tuneify_final_project.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import com.example.tuneify_final_project.ui.adapters.ArtistSpotlightAdapter
import com.example.tuneify_final_project.ui.adapters.ForYouAdapter
import com.example.tuneify_final_project.ui.adapters.ForYouPlaylist
import com.example.tuneify_final_project.ui.adapters.RecentPlaylistAdapter
import com.example.tuneify_final_project.ui.library.LibraryActivity
import com.example.tuneify_final_project.ui.library.PlaylistDetailsActivity
import com.example.tuneify_final_project.ui.models.Artist
import com.example.tuneify_final_project.ui.models.Playlist
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 * HomeActivity is the main screen of the Tuneify application.
 * It displays:
 * - Welcome message for the user
 * - Recently played playlists
 * - "For You" recommended playlists
 * - Artist Spotlight section
 * - Now playing bar
 *
 * It also handles socket communication with the server and manages UI updates
 * based on user session data.
 */
class HomeActivity : AppCompatActivity() {

    private var currentUserId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_home)

        intent.getStringExtra("SESSION_TOKEN")?.let {
            SocketManager.setSessionToken(it)
        }

        NavigationUtils.setupBottomNav(this)
        PlaybackUtils.bind(this)


        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        val savedToken = sharedPref.getString("SESSION_TOKEN", null)
        if (savedToken != null) {
            SocketManager.setSessionToken(savedToken)
            android.util.Log.d("HOME_DEBUG", "Token restored: $savedToken")
        }

        currentUserId    = sharedPref.getInt("USER_ID", -1)
        val firstName    = sharedPref.getString("USER_FIRST_NAME", "User")

        findViewById<TextView>(R.id.tv_home_welcome).text = "Welcome back, $firstName!"



        findViewById<TextView>(R.id.tv_see_all).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }

        setupForYou()
        setupArtistSpotlight()
        fetchRecentPlaylists()
    }

    // Recently Played Playlists

    private fun fetchRecentPlaylists() {
        if (currentUserId == -1) return
        val params = JSONObject().put("user_id", currentUserId)
        SocketManager.sendCommand("GET_USER_PLAYLISTS", params) { response ->
            if (response?.startsWith("OK|") != true) return@sendCommand
            val list = parsePlaylists(response.substringAfter("OK|"))
                .take(5)   // show at most 5 in the row
            runOnUiThread {
                val rv = findViewById<RecyclerView>(R.id.rv_recent_playlists)
                rv.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                rv.adapter = RecentPlaylistAdapter(list) { playlist ->
                    startActivity(Intent(this, PlaylistDetailsActivity::class.java).apply {
                        putExtra("PLAYLIST_ID",    playlist.id)
                        putExtra("PLAYLIST_NAME",  playlist.name)
                        putExtra("PLAYLIST_COVER", playlist.coverUrl)
                    })
                }
            }
        }
    }

    // For You Playlists

    private fun setupForYou() {
        val params = JSONObject().put("user_id", currentUserId)

        SocketManager.sendCommand("GET_FOR_YOU_PLAYLISTS", params) { response ->
            if (response?.startsWith("OK|") != true) return@sendCommand

            val arr = JSONArray(response.substringAfter("OK|"))
            val items = mutableListOf<ForYouPlaylist>()

            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)

                items.add(
                    ForYouPlaylist(
                        id = o.getInt("id"),
                        name = o.getString("name"),
                        subtitle = o.getString("subtitle"),
                        coverUrl = o.optString("cover_url", null)
                    )
                )
            }

            runOnUiThread {
                val rv = findViewById<RecyclerView>(R.id.rv_for_you)
                rv.layoutManager = LinearLayoutManager(this)
                rv.isNestedScrollingEnabled = false

                val adapter = ForYouAdapter(items) { playlist ->
                    startActivity(
                        Intent(this, PlaylistDetailsActivity::class.java).apply {
                            putExtra("PLAYLIST_ID", playlist.id)
                            putExtra("PLAYLIST_NAME", playlist.name)
                            putExtra("PLAYLIST_COVER", playlist.coverUrl)
                        }
                    )
                }

                rv.adapter = adapter
            }
        }
    }

    // Artist Spotlight

    private fun setupArtistSpotlight() {
        data class ArtistCard(val name: String, val genre: String, val photoRes: Int, val activityClass: Class<*>)

        val artists = listOf(
            ArtistCard("Olivia Rodrigo", "Modern Pop",        R.drawable.oliviarodrigo_cover, OliviaRodrigoActivity::class.java),
            ArtistCard("Fleetwood Mac",  "Classic Rock",      R.drawable.fleetwoodmac_cover, FleetwoodMacActivity::class.java),
            ArtistCard("Radiohead",      "Alt / Experimental",R.drawable.radiohead_cover, RadioheadActivity::class.java),
            ArtistCard("Pink Floyd",     "Psychedelic Rock",  R.drawable.pinkfloyd_cover, PinkFloydActivity::class.java),
            ArtistCard("Mazzy Star",     "Dream Pop",         R.drawable.mazzystar_cover, MazzyStarActivity::class.java)
        )

        val artistModels = artists.map { Artist(it.name, it.genre, "", "", it.photoRes, emptyList()) }

        val rv = findViewById<RecyclerView>(R.id.rv_artist_spotlight)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = ArtistSpotlightAdapter(artistModels) { artist ->
            val target = artists.first { it.name == artist.name }.activityClass
            startActivity(Intent(this, target))
        }
    }


    // Input: json (String)
    // Output: List<Playlist>
    private fun parsePlaylists(json: String): List<Playlist> {
        val list = mutableListOf<Playlist>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Playlist(
                    o.getInt("id"),   o.getString("name"),
                    o.getInt("user_id"), o.optString("cover_url", null)
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    override fun onResume() {
        super.onResume()
        NavigationUtils.setupBottomNav(this)
        if (MusicPlayerManager.currentSongTitle != null) {
            PlaybackUtils.updateUI(this)
        } else {
            findViewById<View>(R.id.cv_now_playing_bar)?.visibility = View.GONE
        }
        fetchRecentPlaylists()
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackUtils.unbind(this)
    }
}