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

class SongAdapter(
    private var songs: List<Song>,
    private val onSongClick: (Song) -> Unit,
    // Optional: pass null if you don't want the three-dot (e.g. search results)
    private val onMoreClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle:  TextView  = view.findViewById(R.id.tv_song_title)
        val tvArtist: TextView  = view.findViewById(R.id.tv_song_artist)
        val ivCover:  ImageView = view.findViewById(R.id.iv_song_item_cover)
        val btnMore:  ImageView = view.findViewById(R.id.btn_song_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text  = song.title
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

    override fun getItemCount() = songs.size

    fun updateList(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}