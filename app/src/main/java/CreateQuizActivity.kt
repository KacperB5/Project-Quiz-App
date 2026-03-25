package com.example.mobilequizapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CreateQuizActivity : AppCompatActivity() {

    private lateinit var answers: List<Button>
    private lateinit var progressBar: ProgressBar
    private lateinit var tvQuestion: TextView

    private var currentQuestion = 0
    private var score = 0

    private val handler = Handler(Looper.getMainLooper())
    private var timeLeft = 100

    private val questions = mutableListOf<Question>()

    private var startTimeInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        tvQuestion = findViewById(R.id.tvQuestion)
        progressBar = findViewById(R.id.progressBar)

        val btn1 = findViewById<Button>(R.id.btnAnswer1)
        val btn2 = findViewById<Button>(R.id.btnAnswer2)
        val btn3 = findViewById<Button>(R.id.btnAnswer3)
        val btn4 = findViewById<Button>(R.id.btnAnswer4)

        answers = listOf(btn1, btn2, btn3, btn4)

        answers.forEach { it.isEnabled = false }

        loadQuizFromApi()

        answers.forEachIndexed { index, button ->
            button.setOnClickListener {
                checkAnswer(index)
            }
        }
    }

    private fun loadQuizFromApi() {
        val url = "https://quiz-app.alwaysdata.net/api/quizzes.php"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                for (i in 0 until response.length()) {
                    val obj: JSONObject = response.getJSONObject(i)

                    questions.add(
                        Question(
                            obj.getString("question"),
                            listOf(
                                obj.getString("answer1"),
                                obj.getString("answer2"),
                                obj.getString("answer3"),
                                obj.getString("answer4")
                            ),
                            obj.getInt("correct")
                        )
                    )
                }

                questions.shuffle()

                if (questions.size > 5) {
                    val randomFive = questions.take(5)
                    questions.clear()
                    questions.addAll(randomFive)
                }

                if (questions.isNotEmpty()) {
                    startTimeInMillis = System.currentTimeMillis()
                    loadQuestion()
                } else {
                    Toast.makeText(this, "Brak pytań w bazie danych.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Błąd API: $error", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadQuestion() {
        if (currentQuestion >= questions.size) {
            showScore()
            return
        }

        val q = questions[currentQuestion]

        tvQuestion.text = q.question

        answers.forEachIndexed { i, btn ->
            btn.text = q.answers[i]
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#333333"))
            btn.isEnabled = true
        }

        startTimer()
    }

    private fun checkAnswer(selectedIndex: Int) {
        if (questions.isEmpty() || currentQuestion >= questions.size) return

        stopTimer()

        val correctIndex = questions[currentQuestion].correctIndex

        answers.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            answers[selectedIndex].backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            score++
        } else {
            answers[selectedIndex].backgroundTintList = ColorStateList.valueOf(Color.RED)
            answers[correctIndex].backgroundTintList = ColorStateList.valueOf(Color.GREEN)
        }

        handler.postDelayed({
            currentQuestion++
            loadQuestion()
        }, 2000)
    }

    private fun startTimer() {
        timeLeft = 100
        progressBar.progress = timeLeft

        handler.postDelayed(object : Runnable {
            override fun run() {
                timeLeft--
                progressBar.progress = timeLeft

                if (timeLeft > 0) {
                    handler.postDelayed(this, 100)
                } else {
                    currentQuestion++
                    loadQuestion()
                }
            }
        }, 100)
    }

    private fun stopTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun showScore() {
        setContentView(R.layout.activity_score)

        val endTimeInMillis = System.currentTimeMillis()
        val totalTimeSeconds = ((endTimeInMillis - startTimeInMillis) / 1000).toInt()
        val minutes = totalTimeSeconds / 60
        val seconds = totalTimeSeconds % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        val percentage = if (questions.isNotEmpty()) (score * 100) / questions.size else 0

        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentage)
        val tvTime = findViewById<TextView>(R.id.tvTime)

        tvScore.text = "$score / ${questions.size}"
        tvPercentage.text = "$percentage%"
        tvTime.text = "Czas gry: $timeFormatted"

        val btnReturnMenu = findViewById<Button>(R.id.btnReturnMenu)
        btnReturnMenu.setOnClickListener {
            finish()
        }
    }
}

data class Question(
    val question: String,
    val answers: List<String>,
    val correctIndex: Int
)