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

data class ForYouPlaylist(
    val id: Int,
    val name: String,
    val subtitle: String,
    val coverUrl: String?
)

/**
 * Adapter for the "For You" playlists RecyclerView.
 * Connects a list of ForYouPlaylist objects to the RecyclerView and displays playlist
 * name, subtitle, and cover image (loaded from server using Glide or fallback image).
 */
class ForYouAdapter(
    private var items: List<ForYouPlaylist>,
    private val onClick: (ForYouPlaylist) -> Unit
) : RecyclerView.Adapter<ForYouAdapter.VH>() {

    /**
     * ViewHolder that holds references to the UI elements of a single playlist item.
     */
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_for_you_cover)
        val tvName: TextView = view.findViewById(R.id.tv_for_you_name)
        val tvSubtitle: TextView = view.findViewById(R.id.tv_for_you_subtitle)
    }

    //Input: parent: ViewGroup, viewType: Int
    //Output: VH(ViewHolder)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_for_you_playlist, parent, false)
        return VH(view)
    }

    //Input: holder: VH, position: Int
    //Output: void
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvSubtitle.text = item.subtitle

        val url = item.coverUrl
        if (!url.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load("http://${NetworkConfig.serverIp}:8000/covers/playlist/$url")
                .placeholder(R.drawable.add_playlist_cover)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.add_playlist_cover)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    //Input: none
    //Output: Int
    override fun getItemCount() = items.size

    /**
     * Updates the adapter data and refreshes the RecyclerView.
     *
     * Input: newItems: List<ForYouPlaylist>
     * Output: void
     */
    fun update(newItems: List<ForYouPlaylist>) {
        items = newItems
        notifyDataSetChanged()
    }
}