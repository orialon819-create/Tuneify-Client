package com.example.tuneify_final_project.ui.moodify

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R

/**
 * SCREEN 3 — Mood Result.
 * Routes to MoodSingleSongActivity or MoodPlaylistActivity.
 */
class MoodResultActivity : AppCompatActivity() {

    private data class MoodInfo(val emoji: String, val bgColor: String, val confidence: Int)

    private val moodData = mapOf(
        "Happy"     to MoodInfo("😊", "#E67E22", 85),
        "Sad"       to MoodInfo("😢", "#2980B9", 78),
        "Angry"     to MoodInfo("😠", "#C0392B", 82),
        "Calm"      to MoodInfo("😌", "#27AE60", 80),
        "Energetic" to MoodInfo("⚡", "#8E44AD", 88)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_result)

        val mood = intent.getStringExtra("MOOD") ?: "Calm"
        val info = moodData[mood] ?: MoodInfo("😌", "#27AE60", 80)

        // Dynamic tinted background
        val base = Color.parseColor(info.bgColor)
        findViewById<View>(R.id.mood_bg).setBackgroundColor(
            Color.argb(130, Color.red(base), Color.green(base), Color.blue(base))
        )

        findViewById<TextView>(R.id.tv_mood_emoji).text      = info.emoji
        findViewById<TextView>(R.id.tv_mood_label).text      = "Detected Mood: $mood"
        findViewById<TextView>(R.id.tv_mood_confidence).text = "Confidence: ${info.confidence}%"

        // Back → goes to MoodifyActivity (camera already removed from stack)
        findViewById<ImageView>(R.id.btn_result_back).setOnClickListener { finish() }

        // Play ONE song → dedicated single song player
        findViewById<Button>(R.id.btn_play_song).setOnClickListener {
            startActivity(Intent(this, MoodSingleSongActivity::class.java).apply {
                putExtra("MOOD", mood)
            })
        }

        // Generate PLAYLIST → 5-song playlist screen
        findViewById<Button>(R.id.btn_generate_playlist).setOnClickListener {
            startActivity(Intent(this, MoodPlaylistActivity::class.java).apply {
                putExtra("MOOD", mood)
            })
        }

        // Retake → back to camera (finish this, camera already gone, so goes to MoodifyActivity)
        // User taps Start again to open camera fresh
        findViewById<Button>(R.id.btn_retake).setOnClickListener { finish() }
    }
}