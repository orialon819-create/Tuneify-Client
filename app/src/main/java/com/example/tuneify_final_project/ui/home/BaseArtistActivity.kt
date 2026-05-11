package com.example.tuneify_final_project.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager
import com.example.tuneify_final_project.ui.utils.PlaybackUtils

/**
 * Base class for all 5 artist detail pages.
 * Each subclass provides content + color accent.
 * Handles album carousel, fact swipe cards, track playback.
 */
abstract class BaseArtistActivity : AppCompatActivity() {

    abstract fun layoutRes(): Int
    abstract fun artistName(): String
    abstract fun subLabel(): String
    abstract fun introText(): String
    abstract fun accentColor(): Int
    abstract fun whyTheyMatter(): List<Triple<String, String, String>>
    abstract fun albums(): List<AlbumData>
    abstract fun tracks(): List<TrackData>
    abstract fun facts(): List<FactData>
    abstract fun footerMood(): String
    abstract fun heroImageRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes())

        PlaybackUtils.bind(this)

        findViewById<ImageView>(R.id.iv_hero)?.setImageResource(heroImageRes())
        findViewById<TextView>(R.id.tv_artist_name)?.text = artistName()
        findViewById<TextView>(R.id.tv_sub_label)?.text   = subLabel()
        findViewById<TextView>(R.id.tv_intro)?.text       = introText()
        findViewById<TextView>(R.id.tv_footer_mood)?.text = footerMood()
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { finish() }

        setupWhyTheyMatter()
        setupAlbumCarousel()
        setupTracks()
        setupFacts()
    }

    override fun onResume() {
        super.onResume()
        PlaybackUtils.updateUI(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackUtils.unbind(this)
    }

    private fun setupWhyTheyMatter() {
        val container = findViewById<LinearLayout>(R.id.ll_why_matter) ?: return
        container.removeAllViews()
        for ((emoji, title, desc) in whyTheyMatter()) {
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_why_matter_card, container, false)
            row.findViewById<TextView>(R.id.tv_emoji).text         = emoji
            row.findViewById<TextView>(R.id.tv_matter_title).text  = title
            row.findViewById<TextView>(R.id.tv_matter_desc).text   = desc
            // Tint the circle background with accent color
            row.findViewById<View>(R.id.tv_emoji).parent?.let {
                if (it is FrameLayout) {
                    it.getChildAt(0)?.setBackgroundColor(accentColor() and 0x44FFFFFF.toInt())
                }
            }
            container.addView(row)
        }
    }

    private fun setupAlbumCarousel() {
        val rv = findViewById<RecyclerView>(R.id.rv_albums) ?: return
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = AlbumCarouselAdapter(albums(), accentColor())
    }

    private fun setupTracks() {
        val container = findViewById<LinearLayout>(R.id.ll_tracks) ?: return
        container.removeAllViews()
        for (track in tracks()) {
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_track_card, container, false)
            row.findViewById<TextView>(R.id.tv_track_title).text = track.title
            row.findViewById<TextView>(R.id.tv_track_tag).text   = track.tag

            // Color the circular play button with accent
            row.findViewById<ImageView>(R.id.iv_track_play)
                .setColorFilter(accentColor())

            // Make the circle background tinted with accent
            row.findViewById<View>(R.id.v_play_circle)
                .setBackgroundColor(accentColor() and 0x33FFFFFF.toInt() or 0x22000000)

            row.findViewById<ImageView>(R.id.iv_track_play).setOnClickListener {
                val song = Song(
                    id       = track.streamTitle.hashCode(),
                    title    = track.streamTitle,
                    artist   = artistName(),
                    coverUrl = null
                )
                MusicPlayerManager.playPlaylist(listOf(song), 0)
            }
            container.addView(row)
        }
    }

    private fun setupFacts() {
        val rv = findViewById<RecyclerView>(R.id.rv_facts) ?: return
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = FactCardAdapter(facts())
    }
}

// ── Album Carousel — tracks always visible, tap to highlight ─────────────────

class AlbumCarouselAdapter(
    private val albums: List<AlbumData>,
    private val accentColor: Int
) : RecyclerView.Adapter<AlbumCarouselAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover:   ImageView    = view.findViewById(R.id.iv_album_cover)
        val tvTitle:   TextView     = view.findViewById(R.id.tv_album_title)
        val tvYear:    TextView     = view.findViewById(R.id.tv_album_year)
        val llTracks:  LinearLayout = view.findViewById(R.id.ll_album_tracks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album_card, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val album = albums[position]
        holder.ivCover.setImageResource(album.coverRes)
        holder.tvTitle.text = album.title
        holder.tvYear.text  = album.year
        holder.tvYear.setTextColor(accentColor)

        // Always show track highlights
        holder.llTracks.removeAllViews()
        for (t in album.trackHighlights) {
            val tv = TextView(holder.itemView.context).apply {
                text      = "• $t"
                textSize  = 12f
                setTextColor(0xAAFFFFFF.toInt())
                setPadding(0, 3, 0, 3)
            }
            holder.llTracks.addView(tv)
        }
    }

    override fun getItemCount() = albums.size
}

// ── Fact Card Adapter ─────────────────────────────────────────────────────────

class FactCardAdapter(
    private val facts: List<FactData>
) : RecyclerView.Adapter<FactCardAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tv_fact_emoji)
        val tvFact:  TextView = view.findViewById(R.id.tv_fact_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fact_card, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvEmoji.text = facts[position].emoji
        holder.tvFact.text  = facts[position].fact
    }

    override fun getItemCount() = facts.size
}