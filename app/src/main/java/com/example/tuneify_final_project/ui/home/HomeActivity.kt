package com.example.tuneify_final_project.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.auth.RegisterActivity
import com.example.tuneify_final_project.ui.settings.SettingsActivity
import com.example.tuneify_final_project.ui.utils.PlaybackUtils
import com.example.tuneify_final_project.ui.utils.NavigationUtils
import com.example.tuneify_final_project.ui.utils.MusicPlayerManager


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_home)
        NavigationUtils.setupBottomNav(this)
        PlaybackUtils.bind(this)

        val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
        val firstName = sharedPref.getString("USER_FIRST_NAME", "User")

        val tvWelcome = findViewById<TextView>(R.id.tv_home_welcome)
        tvWelcome.text = "Welcome back, $firstName!"

        val btnSettings = findViewById<ImageView>(R.id.iv_home_settings)



        btnSettings?.setOnClickListener {

            startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))

        }
    }
    override fun onResume() {
        super.onResume()
        NavigationUtils.setupBottomNav(this)

        // Safety Check: If the title is set, force the bar to show
        if (MusicPlayerManager.currentSongTitle != null) {
            PlaybackUtils.updateUI(this)
        } else {
            // This hides it if nothing has ever been played
            findViewById<View>(R.id.cv_now_playing_bar)?.visibility = View.GONE
        }
    }
}