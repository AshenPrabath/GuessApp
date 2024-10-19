package com.example.madguessapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.madguessapp.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NameGeneratorActivity : AppCompatActivity() {
    private lateinit var randomNameTextView: TextView
    private lateinit var userGreetingTextView: TextView
    private lateinit var hintTextview: TextView
    private lateinit var timerTextView: TextView
    private lateinit var marksTextView: TextView

    private lateinit var generateButton: Button
    private lateinit var checkLetterButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var checkWordLengthButton: Button
    private lateinit var logoutButton: Button
    private lateinit var checkButton: Button

    private lateinit var guessInput: EditText
    private lateinit var letterInput: EditText

    private lateinit var userName: String
    private var randomWord: String? = null

    private lateinit var sharedPreferences: SharedPreferences

    private var marks: Int = 100
    private val failCost: Int = 10
    private val letterCost: Int = 5
    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_generator)

        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userName = sharedPreferences.getString("userName", intent.getStringExtra("userName") ?: "User") ?: "User"
        randomNameTextView = findViewById(R.id.randomNameTextView)
        userGreetingTextView = findViewById(R.id.userGreetingTextView)
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
        hintTextview = findViewById(R.id.hintText)

        userGreetingTextView.text = "Hello, $userName!"

        generateRandomName()
        buttonDisableChecker()

        generateButton.setOnClickListener {
            generateRandomName()
            hintTextview.text = "Enter your guess"

        }
        checkButton.setOnClickListener {
            checkGuess()
        }
        leaderboardButton.setOnClickListener {
            val intent = Intent(this@NameGeneratorActivity, LeaderboardActivity::class.java)
            startActivity(intent)
        }
        checkLetterButton.setOnClickListener {
            if (marks >= letterCost) {
                val guessedLetter = letterInput.text.toString().trim().lowercase()

                if (guessedLetter.length == 1) {
                    checkLetter(guessedLetter[0])
                } else {
                    hintTextview.text = "Please enter a single letter."
                }
            } else {
                hintTextview.text = "Not enough marks to check a letter!"
            }
        }
        checkWordLengthButton.setOnClickListener {
            if (marks >= letterCost) {
                checkWordLength()
            } else {
                hintTextview.text = "Not enough marks to check word length!"
            }
        }
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun generateRandomName() {
        CoroutineScope(Dispatchers.IO).launch {
            randomWord = ApiService.getRandomName()
            withContext(Dispatchers.Main) {
                if (randomWord != null) {
                    randomNameTextView.text = "Random word (for debug only): $randomWord"
                    guessInput.setText("")
                    marks = 100
                    guessInput.isEnabled = true
                    checkButton.isEnabled = true
                    checkLetterButton.isEnabled = true
                    checkWordLengthButton.isEnabled = true
                    letterInput.isEnabled = true
                    marksTextView.text = "Remaining Marks: $marks"
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
                hintTextview.text = "Correct! The word was: $randomWord"
                marksTextView.text = "You guessed it! Final marks: $marks"
                timerTextView.text = "Time taken: $timeTaken seconds"
                onCorrectGuess()
            } else {
                marks -= failCost
                marksTextView.text = "Marks: $marks"
                if (marks <= 0) {
                    hintTextview.text = "Game Over! The word was: $randomWord"
                    guessInput.isEnabled = false
                    buttonDisableChecker()
                } else {
                    hintTextview.text = "Wrong! Try again."
                }
            }
        }
    }
    private fun checkLetter(letter: Char) {
        val occurrences = randomWord?.count { it.lowercaseChar() == letter } ?: 0
        marks -= letterCost
        marksTextView.text = "Marks: $marks"

        if (marks <= 0) {
            hintTextview.text = "Game Over! The word was: $randomWord"
            guessInput.isEnabled = false
            buttonDisableChecker()
        } else {
            if (occurrences > 0) {
                hintTextview.text = "The letter '$letter' appears $occurrences times."
            } else {
                hintTextview.text = "The letter '$letter' is not in the word."
            }
        }
    }
    private fun checkWordLength() {
        val wordLength = randomWord?.length ?: 0
        marks -= letterCost
        marksTextView.text = "Marks: $marks"

        if (marks <= 0) {
            hintTextview.text = "Game Over! The word was: $randomWord"
            guessInput.isEnabled = false
            buttonDisableChecker()
        } else {
            hintTextview.text = "The secret word has $wordLength letters."
        }
    }
    private fun onCorrectGuess() {
        val timeTaken = (endTime - startTime) / 1000

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = ApiService.submitScore(userName, marks, timeTaken.toString())

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@NameGeneratorActivity, "Score submitted!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@NameGeneratorActivity, "Failed to submit score", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NameGeneratorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }
    private fun buttonDisableChecker(){
        if (marks === 0) {
            checkButton.isEnabled = false
            checkLetterButton.isEnabled = false
            checkWordLengthButton.isEnabled = false
            letterInput.isEnabled = false

        }
    }
    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this@NameGeneratorActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}