package com.example.mobilequizapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class StatsFragment : Fragment() {

    private lateinit var pbStatsLoading: ProgressBar
    private lateinit var scrollViewStats: ScrollView

    private lateinit var tvGamesPlayedCount: TextView
    private lateinit var tvBestScoreCount: TextView
    private lateinit var tvAvgAccuracyValue: TextView
    private lateinit var tvAvgTimeValue: TextView
    private lateinit var tvFavModeValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        pbStatsLoading = view.findViewById(R.id.pbStatsLoading)
        scrollViewStats = view.findViewById(R.id.scrollViewStats)

        tvGamesPlayedCount = view.findViewById(R.id.tvGamesPlayedCount)
        tvBestScoreCount = view.findViewById(R.id.tvBestScoreCount)
        tvAvgAccuracyValue = view.findViewById(R.id.tvAvgAccuracyValue)
        tvAvgTimeValue = view.findViewById(R.id.tvAvgTimeValue)
        tvFavModeValue = view.findViewById(R.id.tvFavModeValue)

        return view
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }

    private fun loadStatistics() {
        if (!isAdded) return
        val sharedPref = requireActivity().getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Gracz") ?: "Gracz"

        val url = "https://quiz-app.alwaysdata.net/api/get_user_stats.php?username=$username"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                if (!isAdded) return@JsonObjectRequest
                try {
                    val status = response.optString("status", "error")
                    if (status == "success") {
                        val gamesPlayed = response.optInt("games_played", 0)
                        val bestScore = response.optInt("best_score_percent", 0)
                        val avgAccuracy = response.optInt("average_accuracy", 0)
                        val avgTime = response.optDouble("average_time", 5.4)
                        val favMode = response.optString("favorite_mode", "Singleplayer")

                        updateUI(gamesPlayed, bestScore, avgAccuracy, avgTime, favMode)
                    } else {
                        showZeroStats()
                    }
                } catch (e: Exception) {
                    showZeroStats()
                }
            },
            {
                if (!isAdded) return@JsonObjectRequest
                showZeroStats()
            }
        )

        request.setShouldCache(false)
        context?.let {
            Volley.newRequestQueue(it).add(request)
        }
    }

    private fun updateUI(games: Int, best: Int, accuracy: Int, time: Double, mode: String) {
        activity?.runOnUiThread {
            if (!isAdded) return@runOnUiThread

            // Wyświetlanie liczby gier
            tvGamesPlayedCount.text = games.toString()

            // Poprawione, bezpieczne formatowanie procentów wyniku (np. "85%")
            tvBestScoreCount.text = getString(R.string.stats_format_accuracy, best)

            // Dynamiczne szablony językowe z strings.xml
            tvAvgAccuracyValue.text = getString(R.string.stats_format_accuracy, accuracy)
            tvAvgTimeValue.text = getString(R.string.stats_format_time, time)

            // Tłumaczenie ulubionego trybu gry w zależności od języka systemu
            val translatedMode = when (mode.lowercase()) {
                "singleplayer" -> getString(R.string.stats_mode_singleplayer)
                "multiplayer" -> getString(R.string.stats_mode_multiplayer)
                else -> getString(R.string.stats_mode_none)
            }
            tvFavModeValue.text = translatedMode

            pbStatsLoading.visibility = View.GONE
            scrollViewStats.visibility = View.VISIBLE
        }
    }

    private fun showZeroStats() {
        updateUI(0, 0, 0, 0.0, "Brak")
    }
}