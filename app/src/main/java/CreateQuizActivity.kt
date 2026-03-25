package com.example.mobilequizapp

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

        loadQuizFromApi()

        answers.forEachIndexed { index, button ->
            button.setOnClickListener {
                checkAnswer(index)
            }
        }
    }

    private fun loadQuizFromApi() {
        val url = "https://quiz-app.alwaysdata.net/api/get_quiz.php"

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

                loadQuestion()
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
            btn.setBackgroundColor(Color.LTGRAY)
            btn.isEnabled = true
        }

        startTimer()
    }

    private fun checkAnswer(selectedIndex: Int) {
        stopTimer()

        val correctIndex = questions[currentQuestion].correctIndex

        answers.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            answers[selectedIndex].setBackgroundColor(Color.GREEN)
            score++
        } else {
            answers[selectedIndex].setBackgroundColor(Color.RED)
            answers[correctIndex].setBackgroundColor(Color.GREEN)
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

        val tvScore = findViewById<TextView>(R.id.tvScore)
        tvScore.text = "Twój wynik: $score / ${questions.size}"
    }
}

data class Question(
    val question: String,
    val answers: List<String>,
    val correctIndex: Int
)