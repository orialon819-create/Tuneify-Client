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

/**
 * Adapter for selectable song list (used for creating playlists / multi-select actions).
 * Displays songs with checkbox selection, title, artist, and cover image.
 */
class SelectableSongAdapter(private var songs: List<Song>) :
    RecyclerView.Adapter<SelectableSongAdapter.ViewHolder>() {

    /**
     * ViewHolder that holds references to UI elements of a selectable song item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_song_title)
        val tvArtist: TextView = view.findViewById(R.id.tv_song_artist)
        val ivCover: ImageView = view.findViewById(R.id.iv_song_item_cover)
        val cbSelect: CheckBox = view.findViewById(R.id.cb_song_selected)
    }

    //Input: parent: ViewGroup, viewType: Int
    //Output: ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_selectable, parent, false)
        return ViewHolder(view)
    }

    //Input: holder: ViewHolder, position: Int
    //Output: void
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        val imageUrl = "http://${NetworkConfig.serverIp}:8000/covers/song/${song.coverUrl}"
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.ivCover)

        // Checkbox logic
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = song.isSelected
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            song.isSelected = isChecked
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