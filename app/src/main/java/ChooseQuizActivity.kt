package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class ChooseQuizActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_quiz)

        container = findViewById(R.id.quizListContainer)

        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)
        btnBackToMenu.setOnClickListener {
            finish()
        }

        loadQuizzes()
    }

    private fun loadQuizzes() {
        val url = "https://quiz-app.alwaysdata.net/api/get_quiz_list.php"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                container.removeAllViews()
                try {
                    for (i in 0 until response.length()) {
                        val quiz = response.getJSONObject(i)
                        val id = quiz.getInt("id")
                        val title = quiz.getString("title")
                        val author = quiz.getString("author")
                        val count = quiz.getInt("question_count")

                        addQuizCard(id, title, author, count)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Błąd pobierania quizów", Toast.LENGTH_SHORT).show()
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }

    private fun addQuizCard(id: Int, title: String, author: String, count: Int) {
        val view = layoutInflater.inflate(R.layout.item_quiz_card, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tvQuizTitle)
        val tvAuthor = view.findViewById<TextView>(R.id.tvQuizAuthor)
        val tvCount = view.findViewById<TextView>(R.id.tvQuizCount)

        tvTitle.text = title
        tvAuthor.text = "Twórca: $author"
        tvCount.text = "Il. pytań: $count"

        view.setOnClickListener {
            val intent = Intent(this, PlayQuizActivity::class.java)
            intent.putExtra("QUIZ_ID", id)
            startActivity(intent)
        }

        container.addView(view)
    }
}