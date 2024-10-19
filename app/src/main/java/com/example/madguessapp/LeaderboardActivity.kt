package com.example.madguessapp

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madguessapp.model.Entry
import com.example.madguessapp.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val leaderboardTextView: TextView = findViewById(R.id.leaderboardTextView)
        leaderboardTextView.text = "Leaderboards"
        loadLeaderboard()
    }
    private fun loadLeaderboard() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val leaderboard = ApiService.fetchLeaderboard()
                withContext(Dispatchers.Main) {
                    val leaderboardContainer: LinearLayout = findViewById(R.id.leaderboardContainer)
                    if (leaderboard != null) {
                        Toast.makeText(this@LeaderboardActivity, "Fetch successful", Toast.LENGTH_SHORT).show()

                        leaderboardContainer.removeAllViews()

                        for (entry in leaderboard) {
                            val rowLayout = LinearLayout(this@LeaderboardActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }

                            val nameTextView = createTextView(entry.name)
                            val scoreTextView = createTextView(entry.score)
                            val secondsTextView = createTextView(entry.seconds)
                            val dateTextView = createTextView(entry.date)

                            rowLayout.addView(nameTextView)
                            rowLayout.addView(scoreTextView)
                            rowLayout.addView(secondsTextView)
                            rowLayout.addView(dateTextView)

                            leaderboardContainer.addView(rowLayout)
                        }
                    } else {
                        Toast.makeText(this@LeaderboardActivity, "Failed to fetch leaderboard", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LeaderboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }
    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setText(text)
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }
    }

}