package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnWybierz = findViewById<Button>(R.id.btnWybierzQuiz)
        val btnStworz = findViewById<Button>(R.id.btnStworzQuiz)
        val btnRanking = findViewById<Button>(R.id.btnRanking)
        val btnBack = findViewById<Button>(R.id.btnBackToWelcome)

        btnWybierz.setOnClickListener {
            startActivity(Intent(this, ChooseQuizActivity::class.java))
        }

        btnStworz.setOnClickListener {
            startActivity(Intent(this, CreateQuizActivity::class.java))
        }

        btnRanking.setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }
    }
}