package com.example.mobilequizapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private val apiUrl = "https://quiz-app.alwaysdata.net/api/register.php"
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etRegUsername = findViewById<EditText>(R.id.etRegUsername)
        val etRegEmail = findViewById<EditText>(R.id.etRegEmail)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnRegisterSubmit.setOnClickListener {
            val username = etRegUsername.text.toString().trim()
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    registerUser(username, email, password)
                } else {
                    Toast.makeText(this, "Wprowadź poprawny adres e-mail", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(user: String, email: String, pass: String) {
        val formBody = FormBody.Builder()
            .add("username", user)
            .add("email", email) // Dodajemy e-mail do zapytania POST
            .add("password", pass)
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val status = json.getString("status")
                        val message = json.getString("message")
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                            if (status == "success") finish()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "Błąd serwera: brak poprawnej odpowiedzi JSON", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}