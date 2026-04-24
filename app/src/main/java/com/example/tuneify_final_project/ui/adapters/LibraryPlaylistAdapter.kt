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
import com.example.tuneify_final_project.ui.models.Playlist

/**
 * Adapter for the 3-column library grid (matches Spotify "Your Library" style).
 * Uses item_library_playlist.xml which shows a square cover + name + subtitle.
 */
class LibraryPlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistSelected: (Playlist) -> Unit
) : RecyclerView.Adapter<LibraryPlaylistAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_playlist_item_cover)
        val tvName: TextView = view.findViewById(R.id.tv_playlist_name)
        val tvSubtitle: TextView = view.findViewById(R.id.tv_playlist_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_playlist, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.tvName.text = playlist.name
        holder.tvSubtitle.text = "Playlist • You"

        if (!playlist.coverUrl.isNullOrEmpty() && playlist.coverUrl != "null") {
            val imageUrl =
                "http://${NetworkConfig.serverIp}:8000/covers/playlist/${playlist.coverUrl}"
            Glide.with(holder.ivCover.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.add_playlist_cover)
                .error(R.drawable.add_playlist_cover)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.add_playlist_cover)
        }

        holder.itemView.setOnClickListener { onPlaylistSelected(playlist) }
    }

    override fun getItemCount() = playlists.size
}