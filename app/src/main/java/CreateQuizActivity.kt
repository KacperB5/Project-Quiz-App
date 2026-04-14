package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class CreateQuizActivity : AppCompatActivity() {

    private val categoryList = mutableListOf<CategoryInfo>()
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCorrectAnswer: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCorrectAnswer = findViewById(R.id.spinnerCorrectAnswer)
        val btnAddCategory = findViewById<Button>(R.id.btnAddCategory)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitQuestion)
        val btnBack = findViewById<Button>(R.id.btnBackToMenu)

        val etQuestion = findViewById<EditText>(R.id.etNewQuestion)
        val etA1 = findViewById<EditText>(R.id.etAnswer1)
        val etA2 = findViewById<EditText>(R.id.etAnswer2)
        val etA3 = findViewById<EditText>(R.id.etAnswer3)
        val etA4 = findViewById<EditText>(R.id.etAnswer4)

        // Ustawienie Spinnera dla poprawnej odpowiedzi z białym tekstem
        val options = arrayOf("Odpowiedź A", "Odpowiedź B", "Odpowiedź C", "Odpowiedź D")
        val correctAdapter = ArrayAdapter(this, R.layout.spinner_item, options)
        correctAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCorrectAnswer.adapter = correctAdapter

        btnAddCategory.setOnClickListener {
            startActivity(Intent(this, AddQuizActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val q = etQuestion.text.toString().trim()
            val a1 = etA1.text.toString().trim()
            val a2 = etA2.text.toString().trim()
            val a3 = etA3.text.toString().trim()
            val a4 = etA4.text.toString().trim()
            val correct = spinnerCorrectAnswer.selectedItemPosition + 1

            if (q.isEmpty() || a1.isEmpty() || a2.isEmpty() || a3.isEmpty() || a4.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
            } else if (categoryList.isEmpty()) {
                Toast.makeText(this, "Wybierz lub dodaj kategorię!", Toast.LENGTH_SHORT).show()
            } else {
                val selectedCatId = categoryList[spinnerCategory.selectedItemPosition].id
                sendQuestionToDb(selectedCatId, q, a1, a2, a3, a4, correct)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        val url = "https://quiz-app.alwaysdata.net/api/get_quiz_list.php"
        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                categoryList.clear()
                val names = mutableListOf<String>()
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val id = obj.getInt("id")
                        val title = obj.getString("title")
                        categoryList.add(CategoryInfo(id, title))
                        names.add(title)
                    }
                    // Używamy spinner_item dla białego tekstu
                    val adapter = ArrayAdapter(this, R.layout.spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter
                } catch (e: JSONException) { e.printStackTrace() }
            },
            { Toast.makeText(this, "Błąd pobierania kategorii", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun sendQuestionToDb(quizId: Int, q: String, a1: String, a2: String, a3: String, a4: String, correct: Int) {
        val url = "https://quiz-app.alwaysdata.net/api/quizzes.php"
        val request = object : StringRequest(Method.POST, url,
            { Toast.makeText(this, "Dodano pytanie!", Toast.LENGTH_SHORT).show() },
            { Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["quiz_id"] = quizId.toString()
                params["question"] = q
                params["answer1"] = a1
                params["answer2"] = a2
                params["answer3"] = a3
                params["answer4"] = a4
                params["correct"] = correct.toString()
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}

data class CategoryInfo(val id: Int, val title: String)