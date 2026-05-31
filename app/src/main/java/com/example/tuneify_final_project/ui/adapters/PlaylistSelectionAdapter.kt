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
 * Adapter for playlist selection list.
 * Displays available playlists with their name and cover image,
 * and handles user selection.
 */
class PlaylistSelectionAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistSelected: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionAdapter.ViewHolder>() {

    /**
     * ViewHolder that holds references to the UI elements of a single playlist item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_playlist_name)
        val ivCover: ImageView = view.findViewById(R.id.iv_playlist_item_cover)
    }

    //Input: parent: ViewGroup, viewType: Int
    //Output: ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_selection, parent, false)
        return ViewHolder(view)
    }

    //Input: holder: ViewHolder, position: Int
    //Output: void
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.tvName.text = playlist.name

        // Using the /covers/playlist/ path defined in your FastAPI server
        if (!playlist.coverUrl.isNullOrEmpty() && playlist.coverUrl != "null") {
            val imageUrl = "http://${NetworkConfig.serverIp}:8000/covers/playlist/${playlist.coverUrl}"

            Glide.with(holder.ivCover.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.add_playlist_cover)
                .error(R.drawable.add_playlist_cover)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.add_playlist_cover)
        }

        holder.itemView.setOnClickListener {
            onPlaylistSelected(playlist)
        }
    }

    //Input: none
    //Output: Int
    override fun getItemCount() = playlists.size
}