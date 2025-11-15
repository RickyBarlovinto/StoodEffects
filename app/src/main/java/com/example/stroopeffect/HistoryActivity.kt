package com.example.stroopeffect

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRepository: HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyRepository = HistoryRepository(this)

        val historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val noHistoryText = findViewById<TextView>(R.id.noHistoryText)
        val backButton = findViewById<Button>(R.id.backButton)

        val history = historyRepository.getHistory()

        if (history.isEmpty()) {
            noHistoryText.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.GONE
        } else {
            noHistoryText.visibility = View.GONE
            historyRecyclerView.visibility = View.VISIBLE
            historyRecyclerView.layoutManager = LinearLayoutManager(this)
            historyRecyclerView.adapter = HistoryAdapter(history)
        }

        backButton.setOnClickListener {
            finish() // Simply close this activity to go back to the previous one
        }
    }
}
