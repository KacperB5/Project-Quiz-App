package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnStworzQuiz = findViewById<Button>(R.id.btnStworzQuiz)
        val btnWybierzQuiz = findViewById<Button>(R.id.btnWybierzQuiz)

        btnStworzQuiz.setOnClickListener {
            val intent = Intent(this, CreateQuizActivity::class.java)
            startActivity(intent)
        }

        btnWybierzQuiz.setOnClickListener {
            val intent = Intent(this, ChooseQuizActivity::class.java)
            startActivity(intent)
        }
    }
}