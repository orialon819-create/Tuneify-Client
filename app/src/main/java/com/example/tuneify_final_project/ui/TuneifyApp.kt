package com.example.tuneify_final_project.ui

import android.app.Application
import android.content.Context

class TuneifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val prefs = getSharedPreferences("TuneifyPrefs", Context.MODE_PRIVATE)
            prefs.getString("SESSION_TOKEN", null)?.let {
                SocketManager.setSessionToken(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}