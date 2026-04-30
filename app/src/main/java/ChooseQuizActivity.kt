package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ChooseQuizActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var isMultiplayer: Boolean = false
    private var maxPlayers: Int = 2
    private var existingRoomPin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_quiz)

        container = findViewById(R.id.quizListContainer)
        isMultiplayer = intent.getBooleanExtra("IS_MULTIPLAYER", false)
        maxPlayers = intent.getIntExtra("MAX_PLAYERS", 2)
        existingRoomPin = intent.getStringExtra("ROOM_PIN")

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener { finish() }

        loadQuizzes()
    }

    private fun loadQuizzes() {
        val url = "https://quiz-app.alwaysdata.net/api/get_quiz_list.php"
        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                container.removeAllViews()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    addCard(obj.getInt("id"), obj.getString("title"), obj.getString("author"), obj.getInt("question_count"))
                }
            },
            { Toast.makeText(this, "Błąd pobierania", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun addCard(id: Int, title: String, author: String, count: Int) {
        val view = layoutInflater.inflate(R.layout.item_quiz_card, container, false)
        view.findViewById<TextView>(R.id.tvQuizTitle).text = title
        view.findViewById<TextView>(R.id.tvQuizAuthor).text = "Twórca: $author"
        view.findViewById<TextView>(R.id.tvQuizCount).text = "Il. pytań: $count"

        view.setOnClickListener {
            if (isMultiplayer) {
                if (existingRoomPin != null) {
                    startNextRound(id, existingRoomPin!!)
                } else {
                    createRoom(id)
                }
            } else {
                val intent = Intent(this, PlayQuizActivity::class.java)
                intent.putExtra("QUIZ_ID", id)
                startActivity(intent)
            }
        }
        container.addView(view)
    }

    private fun createRoom(quizId: Int) {
        val url = "https://quiz-app.alwaysdata.net/api/create_room.php"
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Host") ?: "Host"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val pin = json.getString("pin")
                    val intent = Intent(this, LobbyActivity::class.java)
                    intent.putExtra("ROOM_PIN", pin)
                    intent.putExtra("QUIZ_ID", quizId)
                    startActivity(intent)
                    finish()
                }
            },
            { Toast.makeText(this, "Błąd tworzenia pokoju", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "username" to username,
                    "quiz_id" to quizId.toString(),
                    "max_players" to maxPlayers.toString()
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun startNextRound(quizId: Int, pin: String) {
        val url = "https://quiz-app.alwaysdata.net/api/next_round.php"
        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val intent = Intent(this, PlayQuizActivity::class.java)
                    intent.putExtra("QUIZ_ID", quizId)
                    intent.putExtra("ROOM_PIN", pin)
                    startActivity(intent)
                    finish()
                }
            },
            { Toast.makeText(this, "Błąd przy zmianie quizu", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("pin" to pin, "quiz_id" to quizId.toString())
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}