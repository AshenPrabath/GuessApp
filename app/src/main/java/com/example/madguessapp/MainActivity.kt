package com.example.madguessapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var proceedButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)

        // Check if name is already stored in SharedPreferences
        val savedName = sharedPreferences.getString("userName", null)
        if (savedName != null) {
            // If a name is stored, skip the input screen and go to NameGeneratorActivity
            val intent = Intent(this@MainActivity, NameGeneratorActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity so it can't be accessed again by back button
            return // Exit onCreate here to prevent loading the input screen
        }

        // If no name is stored, show the name input screen
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.userNameEditText)
        proceedButton = findViewById(R.id.submitButton)

        proceedButton.setOnClickListener {
            val enteredName = nameInput.text.toString().trim()
            if (enteredName.isNotEmpty()) {
                // Save the name in SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("userName", enteredName)
                editor.apply()

                // Proceed to the next activity
                val intent = Intent(this@MainActivity, NameGeneratorActivity::class.java)
                intent.putExtra("userName", enteredName)
                startActivity(intent)
                finish() // Close MainActivity so the user can't go back to it
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}