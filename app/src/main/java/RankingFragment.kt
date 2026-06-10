package com.example.mobilequizapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class RankingFragment : Fragment() {

    private lateinit var rankingListContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)
        rankingListContainer = view.findViewById(R.id.rankingContainer)
        loadRanking()
        return view
    }

    private fun loadRanking() {
        val url = "https://quiz-app.alwaysdata.net/api/get_ranking.php"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                if (!isAdded) return@JsonArrayRequest
                rankingListContainer.removeAllViews()
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
            {
                if (isAdded) {
                    Toast.makeText(context, "Błąd pobierania rankingu", Toast.LENGTH_SHORT).show()
                }
            }
        )
        request.setShouldCache(false)
        context?.let {
            Volley.newRequestQueue(it).add(request)
        }
    }

    private fun addRankingRow(position: Int, username: String, score: Int) {
        val view = layoutInflater.inflate(R.layout.item_ranking, rankingListContainer, false)

        view.findViewById<TextView>(R.id.tvRankPosition).text = "#$position"
        view.findViewById<TextView>(R.id.tvRankName).text = username
        view.findViewById<TextView>(R.id.tvRankScore).text = "$score pkt"

        rankingListContainer.addView(view)
    }
}