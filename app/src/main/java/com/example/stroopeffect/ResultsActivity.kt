package com.example.stroopeffect

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.Serializable
import java.text.DecimalFormat

class ResultsActivity : AppCompatActivity() {

    private var gameData: GameData? = null   // <-- FIXED (Declare it here)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // --- Retrieve gameData from intent ---
        gameData = intent.getSerializableExtra("GAME_DATA") as? GameData
        // If nothing was passed, gameData = null

        // --- Set Text Views ---
        // (Your original stats setup is here)

        // --- Chart Setup ---
        if (gameData != null) {
            setupChart(gameData!!)
        }

        // --- Button Listeners ---
        findViewById<Button>(R.id.restartTestBtn).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("DIFFICULTY", getIntent().getStringExtra("DIFFICULTY"))
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.viewHistoryBtn).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<Button>(R.id.backToMenuBtn).setOnClickListener {
            startActivity(
                Intent(this, FrontPageActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }
    }

    // --- Chart Styling + Setup Function ---
    private fun setupChart(data: GameData) {

        // Example chart setup
        val chart = findViewById<LineChart>(R.id.lineChart)

        val entries = data.timePerQuestion.mapIndexed { index, time ->
            Entry(index.toFloat(), time.toFloat())
        }

        val dataSet = LineDataSet(entries, "Reaction Time (ms)")
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setDrawValues(false)
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.RED)

        chart.data = LineData(dataSet)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false

        chart.invalidate()
    }
}

// --- Your GameData model (example) ---
data class GameData(
    val timePerQuestion: List<Int>,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val averageTime: Double
) : Serializable
