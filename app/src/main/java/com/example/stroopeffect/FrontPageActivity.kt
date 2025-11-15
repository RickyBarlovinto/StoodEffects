package com.example.stroopeffect

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FrontPageActivity : AppCompatActivity() {

    private lateinit var difficultySpinner: Spinner
    private lateinit var wordListEditText: EditText
    private lateinit var colorListEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        difficultySpinner = findViewById(R.id.difficultySpinner)
        wordListEditText = findViewById(R.id.wordList)
        colorListEditText = findViewById(R.id.colorList)

        // Setup Spinner
        val difficulties = arrayOf("Easy", "Medium", "Hard")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        difficultySpinner.adapter = adapter
        difficultySpinner.setSelection(1) // Default to Medium

        findViewById<Button>(R.id.startGameBtn).setOnClickListener {
            val selectedDifficultyValue = when (difficultySpinner.selectedItem.toString()) {
                "Easy" -> "Congruent"
                "Hard" -> "Incongruent"
                else -> "Control Group"
            }
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("DIFFICULTY", selectedDifficultyValue)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.viewHistoryBtn).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<Button>(R.id.applyCustomBtn).setOnClickListener {
            val words = wordListEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val colors = colorListEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (words.size > 1 && colors.size > 1 && words.size == colors.size) {
                // Save to SharedPreferences
                val prefs = getSharedPreferences("stroop_prefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("custom_words", words.joinToString(","))
                    .putString("custom_colors", colors.joinToString(","))
                    .apply()
                Toast.makeText(this, "Custom settings applied!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a matching number of valid words and colors.", Toast.LENGTH_LONG).show()
            }
        }
    }
}