package com.example.tuneify_final_project.ui.utils

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.create_playlist.CreatePlaylistActivity
import com.example.tuneify_final_project.ui.home.HomeActivity
import com.example.tuneify_final_project.ui.library.LibraryActivity
import com.example.tuneify_final_project.ui.moodify.MoodifyActivity
import com.example.tuneify_final_project.ui.search.SearchActivity

object NavigationUtils {
    fun setupBottomNav(activity: Activity) {
        // Find the icons in the current activity layout
        val btnHome = activity.findViewById<ImageView>(R.id.nav_home)
        val btnSearch = activity.findViewById<ImageView>(R.id.nav_search)
        val btnCreate = activity.findViewById<ImageView>(R.id.nav_create)
        val btnLibrary = activity.findViewById<ImageView>(R.id.nav_library)
        val btnMood = activity.findViewById<ImageView>(R.id.nav_mood)

        btnHome?.setOnClickListener {
            if (activity !is HomeActivity) {
                val intent = Intent(activity, HomeActivity::class.java)
                // Crucial: Reorder to front prevents the activity from restarting/resetting data
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
            }
        }

        btnSearch?.setOnClickListener {
            if (activity !is SearchActivity) {
                val intent = Intent(activity, SearchActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
            }
        }

        btnLibrary?.setOnClickListener {
            if (activity !is LibraryActivity){
                val intent = Intent(activity, LibraryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
            }
        }

        btnMood?.setOnClickListener {
            if (activity !is MoodifyActivity) {
                val intent = Intent(activity, MoodifyActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
            }
        }

        btnCreate?.setOnClickListener {
            if (activity !is CreatePlaylistActivity) {
                val intent = Intent(activity, CreatePlaylistActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}