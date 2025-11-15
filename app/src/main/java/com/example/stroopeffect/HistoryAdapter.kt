package com.example.stroopeffect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val history: List<GameResult>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val result = history[position]
        holder.bind(result)
    }

    override fun getItemCount() = history.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val difficultyText: TextView = itemView.findViewById(R.id.difficultyText)
        private val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        private val accuracyText: TextView = itemView.findViewById(R.id.accuracyText)
        private val reactionTimeText: TextView = itemView.findViewById(R.id.reactionTimeText)

        fun bind(result: GameResult) {
            difficultyText.text = "Difficulty: ${result.difficulty}"
            timestampText.text = android.text.format.DateUtils.getRelativeTimeSpanString(result.timestamp)
            scoreText.text = "Score: ${result.score}"
            accuracyText.text = "Accuracy: ${result.accuracy}%"
            reactionTimeText.text = "Avg. Reaction: ${result.avgReactionTime}ms"
        }
    }
}

// Data class to hold the result of a game
data class GameResult(
    val timestamp: Long,
    val difficulty: String,
    val score: Int,
    val accuracy: Int,
    val avgReactionTime: Long
)
