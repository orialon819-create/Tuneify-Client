package com.example.tuneify_final_project.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import com.example.tuneify_final_project.ui.models.Song

class SearchSongAdapter(
    private var songs: List<Song>,
    private val onSongClick: (Song) -> Unit,
    private val onMoreClick: (Song) -> Unit
) : RecyclerView.Adapter<SearchSongAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_song_title)
        val tvArtist: TextView = view.findViewById(R.id.tv_song_artist)
        val btnMore: ImageButton = view.findViewById(R.id.btn_more_options)
        val ivCover: ImageView = view.findViewById(R.id.iv_song_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_search, parent, false)
        return SearchViewHolder(view)
    }

    // FIX: Changed 'SongViewHolder' to 'SearchViewHolder'
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = "Song • ${song.artist}"

        // --- GLIDE IMAGE LOADING ---
        val fileName = song.coverUrl ?: ""
        val imageUrl = "http://${NetworkConfig.serverIp}:8000/covers/song/$fileName"

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.add_playlist_cover)
            .error(R.drawable.add_playlist_cover)
            .centerCrop()
            .into(holder.ivCover)

        // When the row is clicked, play the song
        holder.itemView.setOnClickListener {
            onSongClick(song)
        }

        // When the three dots are clicked, open menu
        holder.btnMore.setOnClickListener {
            onMoreClick(song)
        }
    }

    override fun getItemCount() = songs.size

    fun updateList(newList: List<Song>) {
        songs = newList
        notifyDataSetChanged()
    }
}