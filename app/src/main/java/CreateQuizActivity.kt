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
import org.json.JSONObject

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
        val etMediaUrl = findViewById<EditText>(R.id.etMediaUrl)
        val etA1 = findViewById<EditText>(R.id.etAnswer1)
        val etA2 = findViewById<EditText>(R.id.etAnswer2)
        val etA3 = findViewById<EditText>(R.id.etAnswer3)
        val etA4 = findViewById<EditText>(R.id.etAnswer4)

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
            val mediaUrl = etMediaUrl.text.toString().trim()
            val a1 = etA1.text.toString().trim()
            val a2 = etA2.text.toString().trim()
            val a3 = etA3.text.toString().trim()
            val a4 = etA4.text.toString().trim()
            val correct = spinnerCorrectAnswer.selectedItemPosition + 1

            // Tworzymy listę odpowiedzi zamienionych na małe litery, aby sprawdzić duplikaty niezależnie od wielkości liter
            val answersList = listOf(a1.lowercase(), a2.lowercase(), a3.lowercase(), a4.lowercase())
            // distinct() zostawia tylko unikalne elementy. Jeśli po usunięciu duplikatów lista ma mniej niż 4 elementy, to znaczy, że coś się powtórzyło.
            val hasDuplicateAnswers = answersList.distinct().size < 4

            if (q.isEmpty() || a1.isEmpty() || a2.isEmpty() || a3.isEmpty() || a4.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola pytań i odpowiedzi!", Toast.LENGTH_SHORT).show()
            } else if (categoryList.isEmpty()) {
                Toast.makeText(this, "Wybierz lub dodaj kategorię!", Toast.LENGTH_SHORT).show()
            } else if (hasDuplicateAnswers) {
                // BLOKADA: Wyskakuje błąd o duplikatach odpowiedzi i kod nie idzie dalej
                Toast.makeText(this, "Błąd: Odpowiedzi nie mogą się powtarzać!", Toast.LENGTH_LONG).show()
            } else {
                val selectedCatId = categoryList[spinnerCategory.selectedItemPosition].id
                btnSubmit.isEnabled = false // Blokada podwójnego kliknięcia

                sendQuestionToDb(selectedCatId, q, mediaUrl, a1, a2, a3, a4, correct, btnSubmit) {
                    // Blok kodu wywoływany TYLKO po pomyślnym zapisie w bazie
                    etQuestion.text.clear()
                    etMediaUrl.text.clear()
                    etA1.text.clear()
                    etA2.text.clear()
                    etA3.text.clear()
                    etA4.text.clear()
                }
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
                    val adapter = ArrayAdapter(this, R.layout.spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter
                } catch (e: JSONException) { e.printStackTrace() }
            },
            { Toast.makeText(this, "Błąd pobierania kategorii", Toast.LENGTH_SHORT).show() }
        )
        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }

    private fun sendQuestionToDb(
        quizId: Int, q: String, mediaUrl: String, a1: String, a2: String, a3: String, a4: String, correct: Int,
        btnSubmit: Button, onSuccess: () -> Unit
    ) {
        val url = "https://quiz-app.alwaysdata.net/api/add_question.php"
        val request = object : StringRequest(Method.POST, url,
            { response ->
                btnSubmit.isEnabled = true
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.optString("status")
                    if (status == "success") {
                        Toast.makeText(this, "Dodano pytanie!", Toast.LENGTH_SHORT).show()
                        onSuccess() // Czyszczenie interfejsu
                    } else {
                        val msg = jsonObject.optString("message", "Błąd zapisu")
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show() // Komunikat o duplikacie pytania
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Błąd przetwarzania odpowiedzi", Toast.LENGTH_SHORT).show()
                }
            },
            {
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Błąd połączenia z serwerem", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["quiz_id"] = quizId.toString()
                params["question"] = q
                params["image_url"] = mediaUrl
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