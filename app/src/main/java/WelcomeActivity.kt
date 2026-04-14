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

        // 1. Pobranie nazwy użytkownika z SharedPreferences (zapisanej przy logowaniu)
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Gracz")

        // 2. Wyświetlenie powitania
        val tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        tvWelcomeUser.text = "Witaj, $username!"

        val btnSingleplayer = findViewById<Button>(R.id.btnSingleplayer)
        val btnMultiplayer = findViewById<Button>(R.id.btnMultiplayer)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 3. Przejście do Menu Singleplayer (tam jest wybór i tworzenie)
        btnSingleplayer.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        // 4. Multiplayer (póki co tylko komunikat)
        btnMultiplayer.setOnClickListener {
            android.widget.Toast.makeText(this, "Tryb Multiplayer dostępny wkrótce!", android.widget.Toast.LENGTH_SHORT).show()
        }

        // 5. Wylogowanie
        btnLogout.setOnClickListener {
            // Czyścimy pamięć podręczną logowania
            sharedPref.edit().remove("USERNAME").apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}