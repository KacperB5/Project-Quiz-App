package com.example.mobilequizapp

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class RankingActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        container = findViewById(R.id.rankingContainer)

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener { finish() }

        loadRanking()
    }

    private fun loadRanking() {
        val url = "https://quiz-app.alwaysdata.net/api/get_ranking.php"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                container.removeAllViews()
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val username = obj.getString("username")
                        val score = obj.getInt("total_score")

                        addRankingRow(i + 1, username, score)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { Toast.makeText(this, "Błąd pobierania rankingu", Toast.LENGTH_SHORT).show() }
        )
        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }

    private fun addRankingRow(position: Int, username: String, score: Int) {
        val view = layoutInflater.inflate(R.layout.item_ranking, container, false)

        view.findViewById<TextView>(R.id.tvRankPosition).text = "#$position"
        view.findViewById<TextView>(R.id.tvRankName).text = username
        view.findViewById<TextView>(R.id.tvRankScore).text = "$score pkt"

        container.addView(view)
    }
}