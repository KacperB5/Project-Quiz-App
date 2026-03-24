package com.example.mobilequizapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.widget.EditText
import android.widget.Button
import android.widget.Toast

class CreateQuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val etAnswer1 = findViewById<EditText>(R.id.etAnswer1)
        val etAnswer2 = findViewById<EditText>(R.id.etAnswer2)
        val etAnswer3 = findViewById<EditText>(R.id.etAnswer3)
        val etAnswer4 = findViewById<EditText>(R.id.etAnswer4)
        val etCorrect = findViewById<EditText>(R.id.etCorrect)
        val btnSave = findViewById<Button>(R.id.btnSaveQuiz)

        btnSave.setOnClickListener {

            val url = "https://quiz-app.alwaysdata.net/api/save_quiz.php"

            val request = object : StringRequest(
                Request.Method.POST, url,
                { response ->
                    Toast.makeText(this, "Zapisano quiz!", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    Toast.makeText(this, "Błąd: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["title"] = etTitle.text.toString()
                    params["question"] = etQuestion.text.toString()
                    params["answer1"] = etAnswer1.text.toString()
                    params["answer2"] = etAnswer2.text.toString()
                    params["answer3"] = etAnswer3.text.toString()
                    params["answer4"] = etAnswer4.text.toString()
                    params["correct"] = etCorrect.text.toString()
                    return params
                }
            }

            Volley.newRequestQueue(this).add(request)
        }
    }
}