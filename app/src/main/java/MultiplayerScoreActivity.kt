package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MultiplayerScoreActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private var roomPin: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_score)

        roomPin = intent.getStringExtra("ROOM_PIN") ?: ""
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val myName = sharedPref.getString("USERNAME", "Anonim") ?: "Anonim"

        loadFinalResults(roomPin, myName)
    }

    private fun loadFinalResults(pin: String, myName: String) {
        val url = "https://quiz-app.alwaysdata.net/api/get_room_results.php?pin=$pin"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val data = json.getJSONObject("data")

                    val hName = data.getString("host_name")
                    val hScore = data.getInt("host_score")

                    val gName = data.getString("guest_name")
                    val gScore = data.getInt("guest_score")

                    val g2Name = data.optString("guest2_name", "")
                    val g2Score = data.optInt("guest2_score", 0)

                    findViewById<TextView>(R.id.tvHostName).text = hName
                    findViewById<TextView>(R.id.tvHostScore).text = hScore.toString()

                    findViewById<TextView>(R.id.tvGuestName).text = gName
                    findViewById<TextView>(R.id.tvGuestScore).text = gScore.toString()

                    // Obsługa 3 gracza
                    val tvGuest2Name = findViewById<TextView?>(R.id.tvGuest2Name)
                    val tvGuest2Score = findViewById<TextView?>(R.id.tvGuest2Score)
                    val cardGuest2 = findViewById<View?>(R.id.cardGuest2)

                    if (g2Name.isNotEmpty() && g2Name != "null" && g2Name != "Brak") {
                        cardGuest2?.visibility = View.VISIBLE
                        tvGuest2Name?.text = g2Name
                        tvGuest2Score?.text = g2Score.toString()
                    } else {
                        cardGuest2?.visibility = View.GONE
                    }

                    // Kto wygrał? (Zaktualizowana logika z remisami)
                    val myScore = when (myName) {
                        hName -> hScore
                        gName -> gScore
                        else -> g2Score
                    }
                    val maxScore = maxOf(hScore, gScore, g2Score)

                    // Liczymy, ilu aktywnych graczy ma ten najwyższy wynik
                    var winnersCount = 0
                    if (hScore == maxScore) winnersCount++
                    if (gName.isNotEmpty() && gName != "Brak" && gName != "null" && gScore == maxScore) winnersCount++
                    if (g2Name.isNotEmpty() && g2Name != "Brak" && g2Name != "null" && g2Score == maxScore) winnersCount++

                    val tvWinner = findViewById<TextView>(R.id.tvWinnerText)

                    if (myScore == maxScore) {
                        if (winnersCount > 1) {
                            tvWinner.text = "REMIS! 🤝"
                        } else {
                            tvWinner.text = "WYGRAŁEŚ! 🏆"
                        }
                    } else {
                        tvWinner.text = "PRZEGRAŁEŚ... 💀"
                    }

                    setupButtons(pin, myName == hName)
                }
            }, { Toast.makeText(this, "Błąd pobierania wyników", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun setupButtons(pin: String, isHost: Boolean) {
        val btnNextRound = findViewById<Button>(R.id.btnNextRound)
        val btnLeave = findViewById<Button>(R.id.btnLeave)

        if (isHost) {
            btnNextRound.text = "Wybierz kolejny quiz"
            btnNextRound.setOnClickListener {
                val intent = Intent(this, ChooseQuizActivity::class.java)
                intent.putExtra("IS_MULTIPLAYER", true)
                intent.putExtra("ROOM_PIN", pin)
                startActivity(intent)
                finish()
            }

            btnLeave.text = "Zakończ grę"
            btnLeave.setOnClickListener {
                closeRoom(pin)
            }
        } else {
            btnNextRound.text = "Oczekiwanie na Hosta..."
            btnNextRound.isEnabled = false

            btnLeave.text = "Wyjdź z pokoju"
            btnLeave.setOnClickListener { finish() }

            startPollingForNextRound(pin)
        }
    }

    private fun startPollingForNextRound(pin: String) {
        pollingRunnable = object : Runnable {
            override fun run() {
                val url = "https://quiz-app.alwaysdata.net/api/check_next_round.php?pin=$pin"
                val request = StringRequest(Request.Method.GET, url,
                    { response ->
                        val json = JSONObject(response)
                        if (json.optBoolean("success")) {
                            val nextQuizId = json.getInt("quiz_id")
                            Toast.makeText(this@MultiplayerScoreActivity, "Host wybrał grę! Zaczynamy...", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@MultiplayerScoreActivity, PlayQuizActivity::class.java)
                            intent.putExtra("QUIZ_ID", nextQuizId)
                            intent.putExtra("ROOM_PIN", pin)
                            startActivity(intent)
                            finish()
                        } else if (json.optBoolean("closed")) {
                            Toast.makeText(this@MultiplayerScoreActivity, "Host zdecydował się zakończyć grę.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            handler.postDelayed(this, 2000)
                        }
                    }, {
                        handler.postDelayed(this, 2000)
                    }
                )
                Volley.newRequestQueue(this@MultiplayerScoreActivity).add(request)
            }
        }
        handler.postDelayed(pollingRunnable!!, 2000)
    }

    private fun closeRoom(pin: String) {
        val url = "https://quiz-app.alwaysdata.net/api/close_room.php"
        val request = object : StringRequest(Method.POST, url,
            {
                Toast.makeText(this, "Zakończono grę i zamknięto pokój.", Toast.LENGTH_SHORT).show()
                finish()
            }, {}
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("pin" to pin)
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnable?.let { handler.removeCallbacks(it) }
    }
}