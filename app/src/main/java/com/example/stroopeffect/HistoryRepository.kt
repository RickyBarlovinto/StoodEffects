package com.example.stroopeffect

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepository(context: Context) {

    private val prefs = context.getSharedPreferences("game_history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getHistory(): List<GameResult> {
        val json = prefs.getString("history_list", null) ?: return emptyList()
        val type = object : TypeToken<List<GameResult>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveGameResult(result: GameResult) {
        val history = getHistory().toMutableList()
        history.add(0, result) // Add new result to the top
        val json = gson.toJson(history)
        prefs.edit().putString("history_list", json).apply()
    }
}
