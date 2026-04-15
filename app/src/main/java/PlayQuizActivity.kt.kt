package com.example.mobilequizapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class PlayQuizActivity : AppCompatActivity() {

    private var quizId: Int = 1
    private val questions = mutableListOf<Question>()
    private var currentQuestionIndex = 0
    private var score = 0

    private var startTimeInMillis: Long = 0
    private var questionStartTime: Long = 0
    private val questionTimes = mutableListOf<Long>()

    private var countDownTimer: CountDownTimer? = null

    private lateinit var tvQuestion: TextView
    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_quiz)

        quizId = intent.getIntExtra("QUIZ_ID", 1)

        tvQuestion = findViewById(R.id.tvQuestion)
        btnA = findViewById(R.id.btnAnswer1)
        btnB = findViewById(R.id.btnAnswer2)
        btnC = findViewById(R.id.btnAnswer3)
        btnD = findViewById(R.id.btnAnswer4)
        progressBar = findViewById(R.id.progressBar)

        startTimeInMillis = System.currentTimeMillis()

        loadQuestions()
    }

    private fun loadQuestions() {
        val url = "https://quiz-app.alwaysdata.net/api/quizzes.php?quiz_id=$quizId"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        questions.add(Question(
                            obj.getString("question"),
                            obj.getString("answer1"),
                            obj.getString("answer2"),
                            obj.getString("answer3"),
                            obj.getString("answer4"),
                            obj.getInt("correct")
                        ))
                    }
                    if (questions.isNotEmpty()) {
                        questions.shuffle()
                        if (questions.size > 10) {
                            val subList = questions.take(10)
                            questions.clear()
                            questions.addAll(subList)
                        }

                        displayQuestion()
                    } else {
                        Toast.makeText(this, "Ten quiz nie ma jeszcze pytań!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } catch (e: JSONException) { e.printStackTrace() }
            },
            { Toast.makeText(this, "Błąd pobierania pytań", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun displayQuestion() {
        questionStartTime = System.currentTimeMillis()

        val q = questions[currentQuestionIndex]
        tvQuestion.text = q.text
        btnA.text = q.a1
        btnB.text = q.a2
        btnC.text = q.a3
        btnD.text = q.a4

        val buttons = listOf(btnA, btnB, btnC, btnD)
        val defaultColor = ColorStateList.valueOf(Color.parseColor("#333333"))

        buttons.forEachIndexed { index, button ->
            button.isEnabled = true
            button.backgroundTintList = defaultColor
            button.setOnClickListener { checkAnswer(index, button) }
        }

        countDownTimer?.cancel()

        progressBar.max = 10000
        progressBar.progress = 10000

        countDownTimer = object : CountDownTimer(10000, 20) {
            override fun onTick(millisUntilFinished: Long) {
                progressBar.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                progressBar.progress = 0
                handleTimeOut()
            }
        }.start()
    }

    private fun checkAnswer(selectedIndex: Int, clickedButton: Button) {
        countDownTimer?.cancel()

        val timeSpentOnThisQuestion = System.currentTimeMillis() - questionStartTime
        questionTimes.add(timeSpentOnThisQuestion)

        val correctIndex = questions[currentQuestionIndex].correctIndex
        val buttons = listOf(btnA, btnB, btnC, btnD)

        buttons.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            score++
            clickedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            clickedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            buttons[correctIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        }

        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestionIndex++
            if (currentQuestionIndex < questions.size) {
                displayQuestion()
            } else {
                showFinalScore()
            }
        }, 1500)
    }

    private fun handleTimeOut() {
        questionTimes.add(10000L)

        val correctIndex = questions[currentQuestionIndex].correctIndex
        val buttons = listOf(btnA, btnB, btnC, btnD)

        buttons.forEach { it.isEnabled = false }

        buttons[correctIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))

        Toast.makeText(this, "Czas minął!", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestionIndex++
            if (currentQuestionIndex < questions.size) {
                displayQuestion()
            } else {
                showFinalScore()
            }
        }, 1500)
    }

    private fun showFinalScore() {
        countDownTimer?.cancel()

        val totalTimeMillis = System.currentTimeMillis() - startTimeInMillis
        val totalSeconds = (totalTimeMillis / 1000).toInt()
        val totalFormatted = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)

        val avgTimeSeconds = if (questionTimes.isNotEmpty()) (questionTimes.average() / 1000).toInt() else 0

        setContentView(R.layout.activity_score)

        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentage)
        val tvTime = findViewById<TextView>(R.id.tvTime)

        tvScore.text = "$score / ${questions.size}"
        val percentage = (score * 100) / questions.size
        tvPercentage.text = "$percentage%"

        tvTime.text = "Całkowity czas: $totalFormatted\nŚr. czas/pytanie: ${avgTimeSeconds}s"

        saveResultToDatabase(score, questions.size)

        findViewById<Button>(R.id.btnReturnMenu).setOnClickListener { finish() }
    }

    private fun saveResultToDatabase(finalScore: Int, maxScore: Int) {
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Anonim") ?: "Anonim"
        val url = "https://quiz-app.alwaysdata.net/api/save_result.php"

        val stringRequest = object : StringRequest(Method.POST, url, {}, {}) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["quiz_id"] = quizId.toString()
                params["score"] = finalScore.toString()
                params["max_score"] = maxScore.toString()
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

data class Question(
    val text: String, val a1: String, val a2: String, val a3: String, val a4: String, val correctIndex: Int
)