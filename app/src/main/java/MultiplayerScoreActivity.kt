package com.example.mobilequizapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MultiplayerScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_score)

        val pin = intent.getStringExtra("ROOM_PIN") ?: ""
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val myName = sharedPref.getString("USERNAME", "Anonim") ?: "Anonim"

        loadFinalResults(pin, myName)

        findViewById<Button>(R.id.btnMultiReturn).setOnClickListener {
            finish()
        }
    }

    private fun loadFinalResults(pin: String, myName: String) {
        val url = "https://quiz-app.alwaysdata.net/api/get_room_results.php?pin=$pin"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val data = json.getJSONObject("data")
                    val hName = data.getString("host_name")
                    val gName = data.getString("guest_name")
                    val hScore = data.getInt("host_score")
                    val gScore = data.getInt("guest_score")

                    findViewById<TextView>(R.id.tvHostName).text = hName
                    findViewById<TextView>(R.id.tvGuestName).text = gName
                    findViewById<TextView>(R.id.tvHostScore).text = hScore.toString()
                    findViewById<TextView>(R.id.tvGuestScore).text = gScore.toString()

                    val tvWinner = findViewById<TextView>(R.id.tvWinnerText)

                    val myScore = if (myName == hName) hScore else gScore
                    val opScore = if (myName == hName) gScore else hScore

                    when {
                        myScore > opScore -> tvWinner.text = "WYGRAŁEŚ! 🏆"
                        myScore < opScore -> tvWinner.text = "PRZEGRAŁEŚ... 💀"
                        else -> tvWinner.text = "REMIS! 🤝"
                    }
                }
            }, { Toast.makeText(this, "Błąd pobierania wyników", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }
}