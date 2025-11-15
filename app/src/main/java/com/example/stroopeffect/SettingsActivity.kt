package com.example.stroopeffect

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private lateinit var etWord: EditText
    private lateinit var btnAddWord: Button
    private lateinit var wordsContainer: LinearLayout

    private lateinit var etColor: EditText
    private lateinit var btnAddColor: Button
    private lateinit var colorsContainer: LinearLayout

    private lateinit var previewWord: TextView
    private lateinit var previewSwatches: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button

    private val PREF = "stroop_prefs"
    private val KEY = "stroop_custom_v1"

    private var words = mutableListOf<String>()
    private var colors = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etWord = findViewById(R.id.etWord)
        btnAddWord = findViewById(R.id.btnAddWord)
        wordsContainer = findViewById(R.id.wordsContainer)

        etColor = findViewById(R.id.etColor)
        btnAddColor = findViewById(R.id.btnAddColor)
        colorsContainer = findViewById(R.id.colorsContainer)

        previewWord = findViewById(R.id.previewWord)
        previewSwatches = findViewById(R.id.previewSwatches)
        btnSave = findViewById(R.id.btnSave)
        btnReset = findViewById(R.id.btnReset)

        loadFromPrefs()
        renderWords()
        renderColors()
        updatePreview()

        btnAddWord.setOnClickListener {
            val w = etWord.text.toString().trim()
            if (w.isEmpty()) { toast("Enter a word"); return@setOnClickListener }
            if (words.size >= 7) { toast("Maximum 7 words allowed"); return@setOnClickListener }
            words.add(w.toUpperCase())
            etWord.text.clear()
            renderWords()
            updatePreview()
        }

        btnAddColor.setOnClickListener {
            val c = etColor.text.toString().trim()
            if (c.isEmpty()) { toast("Enter a color (hex or name)"); return@setOnClickListener }
            if (colors.size >= 7) { toast("Maximum 7 colors allowed"); return@setOnClickListener }
            val parsed = tryParseColor(c)
            if (parsed == null) { toast("Invalid color. Use #rrggbb or CSS color name."); return@setOnClickListener }
            val hex = parsed
            colors.add(hex)
            etColor.text.clear()
            renderColors()
            updatePreview()
        }

        btnSave.setOnClickListener { saveToPrefs(); toast("Saved"); finish() }
        btnReset.setOnClickListener { confirmReset() }
    }

    private fun renderWords() {
        wordsContainer.removeAllViews()
        for ((i, w) in words.withIndex()) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            val tv = TextView(this)
            tv.text = w
            tv.textSize = 16f
            tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val btn = Button(this)
            btn.text = "✕"
            btn.setOnClickListener {
                words.removeAt(i)
                renderWords()
                updatePreview()
            }
            row.addView(tv)
            row.addView(btn)
            wordsContainer.addView(row)
        }
        btnAddWord.isEnabled = words.size < 7
    }

    private fun renderColors() {
        colorsContainer.removeAllViews()
        for ((i, c) in colors.withIndex()) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(6,6,6,6)
            row.gravity = Gravity.CENTER_VERTICAL

            val sw = View(this)
            val size = (44 * resources.displayMetrics.density).toInt()
            val lpSw = LinearLayout.LayoutParams(size, size)
            lpSw.setMargins(0,0,12,0)
            sw.layoutParams = lpSw
            try { sw.setBackgroundColor(Color.parseColor(c)) } catch (e: Exception) { sw.setBackgroundColor(Color.LTGRAY) }

            val tv = TextView(this)
            tv.text = c
            tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val btn = Button(this)
            btn.text = "✕"
            btn.setOnClickListener {
                colors.removeAt(i)
                renderColors()
                updatePreview()
            }

            row.addView(sw)
            row.addView(tv)
            row.addView(btn)
            colorsContainer.addView(row)
        }
        btnAddColor.isEnabled = colors.size < 7
    }

    private fun updatePreview() {
        if (words.isNotEmpty()) previewWord.text = words.random() else previewWord.text = "WORD"
        if (colors.isNotEmpty()) {
            try { previewWord.setTextColor(Color.parseColor(colors.random())) } catch (e: Exception) { previewWord.setTextColor(Color.BLACK) }
        } else previewWord.setTextColor(Color.BLACK)

        previewSwatches.removeAllViews()
        for (c in colors) {
            val v = View(this)
            val size = (36 * resources.displayMetrics.density).toInt()
            val lp = LinearLayout.LayoutParams(size, size)
            lp.setMargins(8, 0, 8, 0)
            v.layoutParams = lp
            try { v.setBackgroundColor(Color.parseColor(c)) } catch (e: Exception) { v.setBackgroundColor(Color.LTGRAY) }
            val shape = android.graphics.drawable.GradientDrawable()
            shape.shape = android.graphics.drawable.GradientDrawable.OVAL
            try { shape.setColor(Color.parseColor(c)) } catch (e: Exception) { shape.setColor(Color.LTGRAY) }
            v.background = shape
            previewSwatches.addView(v)
        }
    }

    private fun saveToPrefs() {
        val obj = JSONObject()
        obj.put("words", JSONArray(words))
        obj.put("colors", JSONArray(colors))
        val prefs = getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY, obj.toString()).apply()
    }

    private fun loadFromPrefs() {
        val prefs = getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, null)
        if (json == null) {
            words = mutableListOf("RED","BLUE","GREEN","YELLOW","PINK","PURPLE")
            colors = mutableListOf("#ef4444","#4f46e5","#10b981","#f59e0b","#ec4899","#111827")
            return
        }
        try {
            val obj = JSONObject(json)
            val wa = obj.getJSONArray("words")
            val ca = obj.getJSONArray("colors")
            words = (0 until wa.length()).map { wa.getString(it) }.toMutableList()
            colors = (0 until ca.length()).map { ca.getString(it) }.toMutableList()
        } catch (e: Exception) {
            words = mutableListOf("RED","BLUE","GREEN","YELLOW","PINK","PURPLE")
            colors = mutableListOf("#ef4444","#4f46e5","#10b981","#f59e0b","#ec4899","#111827")
        }
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("Reset to defaults?")
            .setMessage("This will restore default words and colors.")
            .setPositiveButton("Reset") { _, _ ->
                words = mutableListOf("RED","BLUE","GREEN","YELLOW","PINK","PURPLE")
                colors = mutableListOf("#ef4444","#4f46e5","#10b981","#f59e0b","#ec4899","#111827")
                renderWords(); renderColors(); updatePreview()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun tryParseColor(input: String): String? {
        return try {
            val colorInt = Color.parseColor(input.trim())
            String.format("#%06X", 0xFFFFFF and colorInt)
        } catch (e: Exception) {
            null
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
