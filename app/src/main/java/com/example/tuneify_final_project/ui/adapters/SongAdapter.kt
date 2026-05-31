package com.example.tuneify_final_project.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.models.Song

/**
 * Adapter for displaying a list of songs in a RecyclerView.
 * Supports click actions and optional "more options" button per song.
 */
class SongAdapter(
    private var songs: List<Song>,
    private val onSongClick: (Song) -> Unit,
    // Optional: pass null if you don't want the three-dot (e.g. search results)
    private val onMoreClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    /**
     * ViewHolder that holds references to UI elements of a single song item.
     */
    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_song_title)
        val tvArtist: TextView = view.findViewById(R.id.tv_song_artist)
        val ivCover: ImageView = view.findViewById(R.id.iv_song_item_cover)
        val btnMore: ImageView = view.findViewById(R.id.btn_song_more)
    }

    //Input: parent: ViewGroup, viewType: Int
    //Output: SongViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    //Input: holder: SongViewHolder, position: Int
    //Output: void
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        Glide.with(holder.itemView.context)
            .load("http://${NetworkConfig.serverIp}:8000/covers/song/${song.coverUrl}")
            .placeholder(R.drawable.add_playlist_cover)
            .error(R.drawable.add_playlist_cover)
            .into(holder.ivCover)

        holder.itemView.setOnClickListener { onSongClick(song) }

        if (onMoreClick != null) {
            holder.btnMore.visibility = View.VISIBLE
            holder.btnMore.setOnClickListener { onMoreClick.invoke(song) }
        } else {
            holder.btnMore.visibility = View.GONE
        }
    }

    //Input: none
    //Output: Int
    override fun getItemCount() = songs.size

    /**
     * Updates the song list and refreshes the RecyclerView.
     *
     * Input: newSongs: List<Song>
     * Output: void
     */
    fun updateList(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}