package com.example.tuneify_final_project.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.models.Playlist

class PlaylistSelectionAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistSelected: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_playlist_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.tvName.text = playlist.name

        holder.itemView.setOnClickListener {
            onPlaylistSelected(playlist)
        }
    }

    override fun getItemCount() = playlists.size
}