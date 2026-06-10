package com.example.mobilequizapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class AddQuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_quiz)

        val etQuizTitle = findViewById<EditText>(R.id.etQuizTitle)
        val btnCreateQuiz = findViewById<Button>(R.id.btnCreateQuiz)

        btnCreateQuiz.setOnClickListener {
            val title = etQuizTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                btnCreateQuiz.isEnabled = false
                createNewCategory(title, btnCreateQuiz)
            } else {
                Toast.makeText(this, "Wpisz nazwę!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNewCategory(title: String, button: Button) {
        val url = "https://quiz-app.alwaysdata.net/api/add_quiz_category.php"

        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Nieznany") ?: "Nieznany"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                button.isEnabled = true
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.optString("status")
                    if (status == "success") {
                        Toast.makeText(this, "Stworzono quiz: $title", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val message = jsonObject.optString("message", "Błąd zapisu")
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Błąd przetwarzania danych", Toast.LENGTH_SHORT).show()
                }
            },
            {
                button.isEnabled = true
                Toast.makeText(this, "Błąd serwera", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["title"] = title
                params["author"] = username
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}