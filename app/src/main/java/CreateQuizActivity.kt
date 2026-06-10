package com.example.mobilequizapp

import com.example.mobilequizapp.R

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CreateQuizActivity : AppCompatActivity() {

    data class Category(val id: Int, val name: String) {
        override fun toString(): String = name
    }

    private lateinit var cardModeratorPanel: View
    private lateinit var tvModQuestionContent: TextView
    private lateinit var btnApprove: Button
    private lateinit var btnReject: Button

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCorrectAnswer: Spinner
    private lateinit var etNewQuestion: EditText
    private lateinit var etMediaUrl: EditText
    private lateinit var etAnswer1: EditText
    private lateinit var etAnswer2: EditText
    private lateinit var etAnswer3: EditText
    private lateinit var etAnswer4: EditText
    private lateinit var btnSubmitQuestion: Button
    private lateinit var btnBackToMenu: Button
    private lateinit var btnAddCategory: Button

    private var userRole = "user"
    private val pendingQuestions = mutableListOf<JSONObject>()
    private var currentModeratingIndex = 0

    private val categoriesList = mutableListOf<Category>()
    private lateinit var categoriesAdapter: CustomSpinnerAdapter<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        val sharedPref = getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Gracz") ?: "Gracz"

        userRole = sharedPref.getString("ROLE", "user") ?: "user"
        Log.d("QUIZ_API", "Zalogowany użytkownik: $username | Rola pobrana bezpośrednio z bazy: $userRole")

        cardModeratorPanel = findViewById(R.id.cardModeratorPanel)
        tvModQuestionContent = findViewById(R.id.tvModQuestionContent)
        btnApprove = findViewById(R.id.btnApprove)
        btnReject = findViewById(R.id.btnReject)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCorrectAnswer = findViewById(R.id.spinnerCorrectAnswer)
        etNewQuestion = findViewById(R.id.etNewQuestion)
        etMediaUrl = findViewById(R.id.etMediaUrl)
        etAnswer1 = findViewById(R.id.etAnswer1)
        etAnswer2 = findViewById(R.id.etAnswer2)
        etAnswer3 = findViewById(R.id.etAnswer3)
        etAnswer4 = findViewById(R.id.etAnswer4)
        btnSubmitQuestion = findViewById(R.id.btnSubmitQuestion)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        setupSpinners()

        if (userRole == "admin") {
            cardModeratorPanel.visibility = View.VISIBLE
            loadPendingContent()
        } else {
            cardModeratorPanel.visibility = View.GONE
        }

        btnSubmitQuestion.setOnClickListener { submitQuestionPending() }
        btnBackToMenu.setOnClickListener { finish() }
        btnAddCategory.setOnClickListener { showAddCategoryDialog() }

        btnApprove.setOnClickListener { moderateQuestion(true) }
        btnReject.setOnClickListener { moderateQuestion(false) }
    }

    private fun setupSpinners() {
        val correctAdapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_item,
            listOf("Odpowiedź A", "Odpowiedź B", "Odpowiedź C", "Odpowiedź D")
        )
        correctAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerCorrectAnswer.adapter = correctAdapter

        categoriesAdapter = CustomSpinnerAdapter(this, R.layout.spinner_item, categoriesList)
        spinnerCategory.adapter = categoriesAdapter

        loadCategoriesFromServer()
    }

    private fun loadCategoriesFromServer() {
        val url = "https://quiz-app.alwaysdata.net/api/get_quiz_list.php"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d("QUIZ_API", "Otrzymano odpowiedź z serwera: $response")
                try {
                    val tempCategories = mutableListOf<Category>()
                    val trimmed = response.trim()

                    if (trimmed.startsWith("[")) {
                        val array = JSONArray(trimmed)
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            tempCategories.add(Category(obj.getInt("id"), obj.getString("title")))
                        }
                    }

                    if (tempCategories.isNotEmpty()) {
                        categoriesList.clear()
                        categoriesList.addAll(tempCategories)
                        categoriesAdapter.notifyDataSetChanged()
                        Log.d("QUIZ_API", "Pomyślnie załadowano ${tempCategories.size} kategorii")
                    } else {
                        Log.w("QUIZ_API", "Serwer zwrócił pustą listę kategorii.")
                    }
                } catch (e: Exception) {
                    Log.e("QUIZ_API", "Błąd parsowania tablicy JSON: ${e.message}")
                }
            },
            { error ->
                Log.e("QUIZ_API", "Błąd połączenia sieciowego Volley: ${error.message}")
                Toast.makeText(this, "Błąd wczytywania kategorii z serwera", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle("Utwórz nową kategorię quizu")

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (24 * resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2)
        }

        val input = EditText(this).apply {
            hint = "Wpisz nazwę kategorii (np. Sport)"
            setHintTextColor(Color.parseColor("#888888"))
            setTextColor(Color.WHITE)
            setSingleLine(true)
        }

        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Stwórz") { dialog, _ ->
            val title = input.text.toString().trim()
            if (title.isNotEmpty()) {
                submitNewCategory(title)
            } else {
                Toast.makeText(this, "Nazwa kategorii nie może być pusta!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.parseColor("#03DAC5"))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.parseColor("#AAAAAA"))
    }

    private fun submitNewCategory(title: String) {
        val sharedPref = getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "System") ?: "System"

        val url = "https://quiz-app.alwaysdata.net/api/add_quiz_category.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        Toast.makeText(this, "Utworzono kategorię!", Toast.LENGTH_SHORT).show()
                        loadCategoriesFromServer()
                    } else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("QUIZ_API", "Błąd odpowiedzi: ${e.message}")
                    if (response.contains("success")) {
                        Toast.makeText(this, "Utworzono kategorię!", Toast.LENGTH_SHORT).show()
                        loadCategoriesFromServer()
                    }
                }
            },
            { error ->
                Toast.makeText(this, "Błąd połączenia: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "title" to title,
                    "author" to username,
                    "role" to userRole
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun loadPendingContent() {
        val url = "https://quiz-app.alwaysdata.net/api/moderate_content.php?action=get_pending&role=$userRole"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.optString("status", "error")
                    if (status == "success") {
                        val questionsArray = response.getJSONArray("pending_questions")
                        pendingQuestions.clear()
                        for (i in 0 until questionsArray.length()) {
                            pendingQuestions.add(questionsArray.getJSONObject(i))
                        }
                        currentModeratingIndex = 0
                        displayNextPendingQuestion()
                    } else {
                        tvModQuestionContent.text = response.optString("message", "Nieznany błąd bazy danych.")
                        btnApprove.isEnabled = false
                        btnReject.isEnabled = false
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("QUIZ_API", "Błąd pobierania kolejki moderacji: ${error.message}")
                tvModQuestionContent.text = "Wyślij plik moderate_content.php do folderu /api/ na serwerze, aby aktywować panel moderacji."
                btnApprove.isEnabled = false
                btnReject.isEnabled = false
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun displayNextPendingQuestion() {
        if (currentModeratingIndex < pendingQuestions.size) {
            val obj = pendingQuestions[currentModeratingIndex]
            val text = "Pytanie: ${obj.getString("question")}\nA: ${obj.getString("answer1")} | B: ${obj.getString("answer2")}"
            tvModQuestionContent.text = text
            btnApprove.isEnabled = true
            btnReject.isEnabled = true
        } else {
            tvModQuestionContent.text = "Wszystkie pytania zostały zweryfikowane! 🎉"
            btnApprove.isEnabled = false
            btnReject.isEnabled = false
        }
    }

    private fun moderateQuestion(approve: Boolean) {
        if (currentModeratingIndex >= pendingQuestions.size) return

        val obj = pendingQuestions[currentModeratingIndex]
        val questionId = obj.getInt("id")
        val action = if (approve) "approve_question" else "reject_question"
        val url = "https://quiz-app.alwaysdata.net/api/moderate_content.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                        currentModeratingIndex++
                        displayNextPendingQuestion()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                Toast.makeText(this, "Błąd wysyłania decyzji", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "action" to action,
                    "id" to questionId.toString(),
                    "role" to userRole
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun submitQuestionPending() {
        val question = etNewQuestion.text.toString().trim()
        val a1 = etAnswer1.text.toString().trim()
        val a2 = etAnswer2.text.toString().trim()
        val a3 = etAnswer3.text.toString().trim()
        val a4 = etAnswer4.text.toString().trim()
        val mediaUrl = etMediaUrl.text.toString().trim()
        val correct = spinnerCorrectAnswer.selectedItemPosition + 1

        val selectedCategory = spinnerCategory.selectedItem as? Category
        val categoryId = selectedCategory?.id ?: 1

        if (question.isEmpty() || a1.isEmpty() || a2.isEmpty()) {
            Toast.makeText(this, "Wypełnij treść pytania oraz przynajmniej odpowiedzi A i B", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://quiz-app.alwaysdata.net/api/add_question.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("QUIZ_API", "Odpowiedź na dodanie pytania: $response")
                try {
                    val json = JSONObject(response)
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
                    if (json.getString("status") == "success") {
                        etNewQuestion.text.clear()
                        etAnswer1.text.clear()
                        etAnswer2.text.clear()
                        etAnswer3.text.clear()
                        etAnswer4.text.clear()
                        etMediaUrl.text.clear()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (response.contains("success")) {
                        Toast.makeText(this, "Pytanie zostało pomyślnie dodane!", Toast.LENGTH_SHORT).show()
                        etNewQuestion.text.clear()
                        etAnswer1.text.clear()
                        etAnswer2.text.clear()
                        etAnswer3.text.clear()
                        etAnswer4.text.clear()
                        etMediaUrl.text.clear()
                    }
                }
            },
            { error ->
                Toast.makeText(this, "Błąd zapisu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "quiz_id" to categoryId.toString(),
                    "question" to question,
                    "answer1" to a1,
                    "answer2" to a2,
                    "answer3" to a3,
                    "answer4" to a4,
                    "correct" to correct.toString(),
                    "image_url" to mediaUrl,
                    "role" to userRole
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    inner class CustomSpinnerAdapter<T>(
        context: Context,
        resource: Int,
        objects: List<T>
    ) : ArrayAdapter<T>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            if (view is TextView) {
                view.setTextColor(Color.WHITE)
                view.textSize = 16f
            }
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            view.setBackgroundColor(Color.parseColor("#1E1E1E"))
            if (view is TextView) {
                view.setTextColor(Color.WHITE)
                view.textSize = 16f
                view.setPadding(40, 40, 40, 40)
            }
            return view
        }
    }
}