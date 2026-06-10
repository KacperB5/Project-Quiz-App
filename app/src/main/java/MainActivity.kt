package com.example.mobilequizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("USERNAME", null)
        if (savedUsername != null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)

        btnLogin.setOnClickListener { performLogin() }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://quiz-app.alwaysdata.net/api/login.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("QUIZ_API_LOGIN", "Odpowiedź serwera: $response")
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")
                    val message = json.getString("message")

                    if (status == "success") {
                        val serverUsername = json.getString("username")
                        val serverRole = json.getString("role")

                        val sharedPref = getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("USERNAME", serverUsername)
                            putString("ROLE", serverRole)
                            apply()
                        }

                        Toast.makeText(this, "Witaj, $serverUsername!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Błąd przetwarzania odpowiedzi serwera.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode
                Log.e("QUIZ_API_LOGIN", "Błąd sieci: ${error.message} | Kod statusu HTTP: $statusCode")

                val userFriendlyMessage = when (statusCode) {
                    404 -> "Nie znaleziono pliku login.php na serwerze FTP (Błąd 404)."
                    500 -> "Wewnętrzny błąd serwera PHP (Błąd 500). Sprawdź składnię pliku."
                    else -> "Błąd połączenia z serwerem. Upewnij się, że masz połączenie z Internetem."
                }
                Toast.makeText(this, userFriendlyMessage, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "username" to username,
                    "password" to password
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}