package com.example.tuneify_final_project.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.models.Artist

class ArtistSpotlightAdapter(
    private val artists: List<Artist>,
    private val onClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistSpotlightAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto:  ImageView = view.findViewById(R.id.iv_artist_photo)
        val tvName:   TextView  = view.findViewById(R.id.tv_artist_name)
        val tvGenre:  TextView  = view.findViewById(R.id.tv_artist_genre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist_spotlight, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val artist = artists[position]
        holder.tvName.text  = artist.name
        holder.tvGenre.text = artist.genre
        holder.ivPhoto.setImageResource(artist.photoResId)
        holder.itemView.setOnClickListener { onClick(artist) }
    }

    override fun getItemCount() = artists.size
}