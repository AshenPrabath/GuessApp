package com.example.madguessapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madguessapp.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NameGeneratorActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userName: String
    private lateinit var randomNameTextView: TextView
    private lateinit var userGreetingTextView: TextView
    private lateinit var generateButton: Button
    private lateinit var guessInput: EditText
    private lateinit var checkLetterButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var letterInput: EditText
    private lateinit var checkWordLengthButton: Button
    private lateinit var logoutButton: Button
    private lateinit var timerTextView: TextView
    private var randomWord: String? = null
    private lateinit var checkButton: Button
    private lateinit var marksTextView: TextView
    private var marks: Int = 100
    private val failCost: Int = 10
    private val letterCost: Int = 5
    private val lengthCost: Int = 5
    private var startTime: Long = 0 // Variable to track start time
    private var endTime: Long = 0 // Variable to track end time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_generator)

        randomNameTextView = findViewById(R.id.randomNameTextView)
        userGreetingTextView = findViewById(R.id.userGreetingTextView)
        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userName = sharedPreferences.getString("userName", intent.getStringExtra("userName") ?: "User") ?: "User"
        generateButton = findViewById(R.id.generateButton)
        guessInput = findViewById(R.id.guessInput)
        checkButton = findViewById(R.id.checkButton)
        logoutButton = findViewById(R.id.logoutButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)
        marksTextView = findViewById(R.id.marksTextView)
        checkLetterButton = findViewById(R.id.checkLetterButton)
        letterInput = findViewById(R.id.letterInput)
        checkWordLengthButton = findViewById(R.id.checkWordLengthButton)
        timerTextView = findViewById(R.id.timerTextView)

        userGreetingTextView.text = "Hello, $userName!"

        generateRandomName()

        generateButton.setOnClickListener {
            generateRandomName()
        }
        checkButton.setOnClickListener {
            checkGuess() // Call the method to check the user's guess
        }
        leaderboardButton.setOnClickListener {
            val intent = Intent(this@NameGeneratorActivity, LeaderboardActivity::class.java)
            startActivity(intent)
        }
        checkLetterButton.setOnClickListener {
            if (marks >= letterCost) {
                val guessedLetter = letterInput.text.toString().trim().lowercase()

                if (guessedLetter.length == 1) {
                    checkLetter(guessedLetter[0]) // Call the function to check letter
                } else {
                    randomNameTextView.text = "Please enter a single letter."
                }
            } else {
                randomNameTextView.text = "Not enough marks to check a letter!"
            }
        }
        checkWordLengthButton.setOnClickListener {
            if (marks >= letterCost) {
                checkWordLength()
            } else {
                randomNameTextView.text = "Not enough marks to check word length!"
            }
        }
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun generateRandomName() {
        CoroutineScope(Dispatchers.IO).launch {
            // Call the getRandomWord() function from ApiService
            randomWord = ApiService.getRandomName()
            withContext(Dispatchers.Main) {
                if (randomWord != null) {
                    randomNameTextView.text = "Hello, $userName! Your random word is: $randomWord"
                    guessInput.setText("") // Clear the input field after generating a new word
                    marks = 100
                    guessInput.isEnabled = true
                    checkButton.isEnabled = true
                    checkLetterButton.isEnabled = true
                    checkWordLengthButton.isEnabled = true
                    marksTextView.text = "Marks: $marks" // Reset marks display
                    startTime = System.currentTimeMillis()
                    timerTextView.text = "Timer started!"
                } else {
                    randomNameTextView.text = "Error fetching word"
                }
            }
        }
    }

    private fun checkGuess() {
        val userGuess = guessInput.text.toString().trim()
        if (marks > 0) {
            if (userGuess.equals(randomWord, ignoreCase = true)) {
                endTime = System.currentTimeMillis()
                val timeTaken = (endTime - startTime) / 1000
                randomNameTextView.text = "Correct! The word was: $randomWord"
                marksTextView.text = "You guessed it! Final marks: $marks"
                timerTextView.text = "Time taken: $timeTaken seconds"
                onCorrectGuess()
            } else {
                marks -= failCost // Deduct marks
                marksTextView.text = "Marks: $marks" // Update marks display
                if (marks <= 0) {
                    randomNameTextView.text = "Game Over! The word was: $randomWord"
                    guessInput.isEnabled = false // Disable input
                    checkButton.isEnabled = false // Disable check button
                } else {
                    randomNameTextView.text = "Wrong! Try again."
                }
            }
        }
    }
    private fun checkLetter(letter: Char) {
        val occurrences = randomWord?.count { it.lowercaseChar() == letter } ?: 0
        marks -= letterCost // Deduct the cost for checking a letter
        marksTextView.text = "Marks: $marks"

        if (marks <= 0) {
            randomNameTextView.text = "Game Over! The word was: $randomWord"
            guessInput.isEnabled = false
            checkButton.isEnabled = false
            checkLetterButton.isEnabled = false
        } else {
            if (occurrences > 0) {
                randomNameTextView.text = "The letter '$letter' appears $occurrences times."
            } else {
                randomNameTextView.text = "The letter '$letter' is not in the word."
            }
        }
    }
    private fun checkWordLength() {
        val wordLength = randomWord?.length ?: 0
        marks -= letterCost // Deduct the cost for checking word length
        marksTextView.text = "Marks: $marks"

        if (marks <= 0) {
            randomNameTextView.text = "Game Over! The word was: $randomWord"
            guessInput.isEnabled = false
            checkButton.isEnabled = false
            checkLetterButton.isEnabled = false
            checkWordLengthButton.isEnabled = false
        } else {
            randomNameTextView.text = "The secret word has $wordLength letters."
        }
    }
    private fun onCorrectGuess() {
        val timeTaken = (endTime - startTime) / 1000

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Attempt to submit the score
                val success = ApiService.submitScore(userName, marks, timeTaken.toString())

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@NameGeneratorActivity, "Score submitted!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@NameGeneratorActivity, "Failed to submit score", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that occurred during the API call
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NameGeneratorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()  // Print the stack trace for debugging
            }
        }
    }
    private fun logout() {
        // Clear the stored username from SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect back to MainActivity
        val intent = Intent(this@NameGeneratorActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Close the current activity so the user can't go back to it
    }
}