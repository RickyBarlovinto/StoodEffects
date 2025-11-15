package com.example.stroopeffect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.Serializable
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    // UI
    private lateinit var timeTv: TextView
    private lateinit var scoreTv: TextView
    private lateinit var accuracyTv: TextView
    private lateinit var wordTv: TextView
    private lateinit var swatchesContainer: LinearLayout
    private lateinit var previewRow: LinearLayout
    private lateinit var startBtn: Button
    private lateinit var resetBtn: Button
    private lateinit var settingsBtn: Button

    // Game State
    private var words = mutableListOf<String>()
    private var colors = mutableListOf<String>()
    private var timeLeft = 30
    private var score = 0
    private var attempts = 0
    private var correctColor: String? = null
    private var running = false
    private var timer: CountDownTimer? = null

    // Prefs
    private val PREF = "stroop_prefs"
    private val KEY = "stroop_custom_v1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timeTv = findViewById(R.id.timeTv)
        scoreTv = findViewById(R.id.scoreTv)
        accuracyTv = findViewById(R.id.accuracyTv)
        wordTv = findViewById(R.id.wordTv)
        swatchesContainer = findViewById(R.id.swatchesContainer)
        previewRow = findViewById(R.id.previewRow)
        startBtn = findViewById(R.id.startBtn)
        resetBtn = findViewById(R.id.resetBtn)
        settingsBtn = findViewById(R.id.settingsBtn)

        resetBtn.isEnabled = false

        loadCustomSettings()
        renderSwatches()
        updatePreviewRow()

        startBtn.setOnClickListener { startGame() }
        resetBtn.setOnClickListener { resetGame() }
        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadCustomSettings()
        renderSwatches()
        updatePreviewRow()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    private fun startGame() {
        if (running) return
        running = true
        score = 0
        attempts = 0
        timeLeft = 30
        updateStats()
        startBtn.isEnabled = false
        resetBtn.isEnabled = true
        nextRound()
        timer = object : CountDownTimer((timeLeft * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
                timeTv.text = timeLeft.toString()
            }

            override fun onFinish() {
                endGame()
            }
        }.start()
    }

    private fun resetGame() {
        running = false
        timer?.cancel()
        startBtn.isEnabled = true
        resetBtn.isEnabled = false
        score = 0
        attempts = 0
        timeLeft = 30
        updateStats()
        wordTv.text = "WORD"
        wordTv.setTextColor(Color.parseColor("#111827"))
    }

    private fun endGame() {
        running = false
        timer?.cancel()
        startBtn.isEnabled = true
        resetBtn.isEnabled = false
        Toast.makeText(this, "Game over! Score: $score / $attempts", Toast.LENGTH_LONG).show()
    }

    private fun nextRound() {
        val w = if (words.isNotEmpty()) words.random() else listOf("RED", "BLUE").random()
        val ink = if (colors.isNotEmpty()) colors.random() else listOf("#ef4444", "#4f46e5").random()
        wordTv.text = w
        try {
            wordTv.setTextColor(Color.parseColor(ink))
        } catch (e: Exception) { wordTv.setTextColor(Color.BLACK) }
        correctColor = ink
    }

    private fun handleChoice(colorPicked: String) {
        if (!running) return
        attempts++
        if (colorPicked == correctColor) {
            score++
        }
        updateStats()
        nextRound()
    }

    private fun updateStats() {
        timeTv.text = timeLeft.toString()
        scoreTv.text = score.toString()
        val acc = if (attempts == 0) 0 else (score * 100 / attempts)
        accuracyTv.text = "${acc}%"
    }

    private fun renderSwatches() {
        swatchesContainer.removeAllViews()
        val displayColors = if (colors.size >= 3) colors else listOf("#ef4444", "#4f46e5", "#10b981")
        for (c in displayColors) {
            val v = createSwatchView(c, 84)
            v.setOnClickListener { handleChoice(c) }
            swatchesContainer.addView(v)
        }
    }

    private fun updatePreviewRow() {
        previewRow.removeAllViews()
        val displayColors = if (colors.isNotEmpty()) colors else listOf("#ef4444", "#4f46e5", "#10b981")
        for (c in displayColors) {
            val v = createSwatchView(c, 44)
            previewRow.addView(v)
        }
    }

    private fun createSwatchView(colorHex: String, sizeDp: Int): View {
        val sizePx = (sizeDp * resources.displayMetrics.density).toInt()
        val bg = View(this)
        val lp = LinearLayout.LayoutParams(sizePx, sizePx)
        lp.setMargins(12, 0, 12, 0)
        bg.layoutParams = lp
        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.OVAL
        try { shape.setColor(Color.parseColor(colorHex)) } catch (e: Exception) { shape.setColor(Color.LTGRAY) }
        bg.background = shape
        return bg
    }

    private fun loadCustomSettings() {
        val prefs = getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, null)
        if (json == null) {
            words = mutableListOf("RED","BLUE","GREEN","YELLOW","PINK","PURPLE")
            colors = mutableListOf("#ef4444","#4f46e5","#10b981","#f59e0b","#ec4899","#111827")
            return
        }
        try {
            val obj = JSONObject(json)
            val wArr = obj.getJSONArray("words")
            val cArr = obj.getJSONArray("colors")
            words = (0 until wArr.length()).map { wArr.getString(it) }.toMutableList()
            colors = (0 until cArr.length()).map { cArr.getString(it) }.toMutableList()
        } catch (e: Exception) {
            words = mutableListOf("RED","BLUE","GREEN","YELLOW","PINK","PURPLE")
            colors = mutableListOf("#ef4444","#4f46e5","#10b981","#f59e0b","#ec4899","#111827")
        }
    }

    data class ColorEntry(val name: String, val value: Int) : Serializable
}