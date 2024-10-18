package com.example.madguessapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val leaderboardTextView: TextView = findViewById(R.id.leaderboardTextView)
        leaderboardTextView.text = "Leaderboards"
    }
}