package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Gracz")

        val tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        tvWelcomeUser.text = "Witaj, $username!"

        val btnSingleplayer = findViewById<Button>(R.id.btnSingleplayer)
        val btnMultiPlayer = findViewById<Button>(R.id.btnMultiPlayer)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnSingleplayer.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        btnMultiPlayer.setOnClickListener {
            startActivity(Intent(this, MultiplayerActivity::class.java))
        }

        btnLogout.setOnClickListener {
            sharedPref.edit().remove("USERNAME").apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}