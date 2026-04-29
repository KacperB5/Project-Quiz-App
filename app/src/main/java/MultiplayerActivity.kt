package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MultiplayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        val btnCreateRoom = findViewById<Button>(R.id.btnCreateRoom)
        val btnJoinRoom = findViewById<Button>(R.id.btnJoinRoom)
        val etRoomPin = findViewById<EditText>(R.id.etRoomPin)
        val btnBack = findViewById<Button>(R.id.btnBackToMenu)

        btnBack.setOnClickListener { finish() }

        btnCreateRoom.setOnClickListener {
            val intent = Intent(this, ChooseQuizActivity::class.java)
            intent.putExtra("IS_MULTIPLAYER", true)
            startActivity(intent)
        }

        btnJoinRoom.setOnClickListener {
            val pin = etRoomPin.text.toString().trim()
            if (pin.length == 4) {
                joinRoom(pin)
            } else {
                Toast.makeText(this, "PIN musi mieć dokładnie 4 cyfry!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun joinRoom(pin: String) {
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Anonim") ?: "Anonim"

        val url = "https://quiz-app.alwaysdata.net/api/join_room.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        val quizId = jsonResponse.getInt("quiz_id")
                        Toast.makeText(this, "Dołączono! Odpalam quiz...", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, PlayQuizActivity::class.java)
                        intent.putExtra("QUIZ_ID", quizId)
                        intent.putExtra("ROOM_PIN", pin)
                        startActivity(intent)
                    } else {
                        val errorMsg = jsonResponse.getString("error")
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Błąd odczytu danych z serwera", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Błąd sieci!", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["pin"] = pin
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}