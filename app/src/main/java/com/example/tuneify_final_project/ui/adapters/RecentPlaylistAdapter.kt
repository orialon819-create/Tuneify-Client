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

class RecentPlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<RecentPlaylistAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_recent_cover)
        val tvTitle: TextView  = view.findViewById(R.id.tv_recent_title)
        val tvArtist: TextView = view.findViewById(R.id.tv_recent_artist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_playlist, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = playlists[position]
        holder.tvTitle.text  = p.name
        holder.tvArtist.text = "Playlist"

        if (!p.coverUrl.isNullOrEmpty() && p.coverUrl != "null") {
            Glide.with(holder.ivCover.context)
                .load("http://${NetworkConfig.serverIp}:8000/covers/playlist/${p.coverUrl}")
                .centerCrop()
                .placeholder(R.drawable.add_playlist_cover)
                .error(R.drawable.add_playlist_cover)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.add_playlist_cover)
        }

        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount() = playlists.size
}