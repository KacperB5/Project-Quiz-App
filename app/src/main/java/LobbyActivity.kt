package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LobbyActivity : AppCompatActivity() {

    private lateinit var pin: String
    private var quizId: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        pin = intent.getStringExtra("ROOM_PIN") ?: ""
        quizId = intent.getIntExtra("QUIZ_ID", 0)

        findViewById<TextView>(R.id.tvLobbyPin).text = pin

        checkRunnable = Runnable {
            checkStatus()
            handler.postDelayed(checkRunnable, 3000)
        }
        handler.post(checkRunnable)

        val btnCancel = findViewById<Button>(R.id.btnCancelWaiting)
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun checkStatus() {
        val url = "https://quiz-app.alwaysdata.net/api/check_room_status.php?pin=$pin"
        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                if (json.optString("status") == "playing") {
                    handler.removeCallbacks(checkRunnable)
                    val intent = Intent(this, PlayQuizActivity::class.java)
                    intent.putExtra("QUIZ_ID", quizId)
                    intent.putExtra("ROOM_PIN", pin)
                    startActivity(intent)
                    finish()
                }
            }, {}
        )
        Volley.newRequestQueue(this).add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
    }
}