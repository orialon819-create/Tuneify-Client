package com.example.tuneify_final_project.ui.home

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.utils.PlaybackUtils

class ArtistDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_detail)

        PlaybackUtils.bind(this)

        val name     = intent.getStringExtra("ARTIST_NAME")  ?: ""
        val meta     = intent.getStringExtra("ARTIST_META")  ?: ""
        val bio      = intent.getStringExtra("ARTIST_BIO")   ?: ""
        val photoRes = intent.getIntExtra("ARTIST_PHOTO_RES", R.drawable.add_playlist_cover)

        findViewById<ImageView>(R.id.iv_artist_hero).setImageResource(photoRes)
        findViewById<TextView>(R.id.tv_artist_name).text = name
        findViewById<TextView>(R.id.tv_artist_meta).text = meta
        findViewById<TextView>(R.id.tv_artist_bio).text  = bio
        findViewById<ImageView>(R.id.btn_artist_back).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        PlaybackUtils.updateUI(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackUtils.unbind(this)
    }
}