package com.example.tuneify_final_project.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.models.Song
import com.example.tuneify_final_project.ui.NetworkConfig


class SongAdapter(private var songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // The ViewHolder "holds" the views for a single row so Android doesn't have to keep looking them up
    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_song_title)
        val tvArtist: TextView = view.findViewById(R.id.tv_song_artist)
        val cbSelect: CheckBox = view.findViewById(R.id.cb_song_selected)
        val ivCover: ImageView = view.findViewById(R.id.iv_song_item_cover)
    }

    // This creates the actual row layout on the screen
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_selectable, parent, false)
        return SongViewHolder(view)
    }

    // This puts the data (Title, Artist) into the TextViews of the row
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        // --- GLIDE IMAGE LOADING ---
        // We use the filename from the song object.
        // If it's null, we pass an empty string so the placeholder shows.
        val fileName = song.coverUrl ?: ""
        val imageUrl = "http://${NetworkConfig.serverIp}:8000/covers/song/$fileName"

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.add_playlist_cover) // Show this while loading
            .error(R.drawable.add_playlist_cover)       // Show this if image doesn't exist
            .centerCrop()
            .into(holder.ivCover)
        // ----------------------------

        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = song.isSelected

        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            song.isSelected = isChecked
        }
    }

    override fun getItemCount() = songs.size

    // This function will be called when search results come back from Python
    fun updateList(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged() // Refreshes the list on the screen
    }
}