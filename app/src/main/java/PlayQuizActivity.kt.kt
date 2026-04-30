package com.example.mobilequizapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONException
import org.json.JSONObject

class PlayQuizActivity : AppCompatActivity() {

    private var quizId: Int = 1
    private var roomPin: String? = null

    private val questions = mutableListOf<Question>()
    private var currentQuestionIndex = 0
    private var score = 0

    private var startTimeInMillis: Long = 0
    private var questionStartTime: Long = 0
    private val questionTimes = mutableListOf<Long>()
    private var countDownTimer: CountDownTimer? = null

    private val syncHandler = Handler(Looper.getMainLooper())
    private var syncRunnable: Runnable? = null

    private var waitingForOpponentSeconds = 0

    private lateinit var tvQuestion: TextView
    private lateinit var ivQuestionImage: ImageView
    private lateinit var wvYoutube: WebView
    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_quiz)

        quizId = intent.getIntExtra("QUIZ_ID", 1)
        roomPin = intent.getStringExtra("ROOM_PIN")

        tvQuestion = findViewById(R.id.tvQuestion)
        ivQuestionImage = findViewById(R.id.ivQuestionImage)
        wvYoutube = findViewById(R.id.wvYoutube)
        btnA = findViewById(R.id.btnAnswer1)
        btnB = findViewById(R.id.btnAnswer2)
        btnC = findViewById(R.id.btnAnswer3)
        btnD = findViewById(R.id.btnAnswer4)
        progressBar = findViewById(R.id.progressBar)

        wvYoutube.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        wvYoutube.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    view?.loadUrl("javascript:(function() { if(document.getElementsByTagName('video')[0]) document.getElementsByTagName('video')[0].muted = false; })()")
                }, 100)
            }
        }

        wvYoutube.webChromeClient = WebChromeClient()

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

                        val imgRaw = obj.optString("image_url", "")
                        val imgUrl = if (imgRaw.isNotEmpty() && imgRaw != "null") imgRaw else null

                        questions.add(Question(
                            obj.optString("question", ""),
                            imgUrl,
                            obj.optString("answer1", ""),
                            obj.optString("answer2", ""),
                            obj.optString("answer3", ""),
                            obj.optString("answer4", ""),
                            obj.optInt("correct", 1) - 1
                        ))
                    }
                    if (questions.isNotEmpty()) {
                        if (roomPin == null) {
                            questions.shuffle()
                        }

                        if (questions.size > 10) {
                            val subList = questions.take(10)
                            questions.clear()
                            questions.addAll(subList)
                        }

                        displayQuestion()
                    } else {
                        tvQuestion.text = "Ten quiz nie ma jeszcze pytań!"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    tvQuestion.text = "Błąd czytania danych z serwera."
                }
            },
            {
                tvQuestion.text = "Błąd połączenia z serwerem!"
            }
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

        val link = q.imageUrl ?: ""

        if (link.contains(".mp4", ignoreCase = true)) {
            ivQuestionImage.visibility = View.GONE
            wvYoutube.visibility = View.VISIBLE

            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        body { margin: 0; padding: 0; background-color: #1E1E1E; display: flex; justify-content: center; align-items: center; height: 100vh; overflow: hidden; }
                        video { max-width: 100vw; max-height: 100vh; outline: none; }
                    </style>
                </head>
                <body>
                    <video autoplay muted playsinline controls controlsList="nodownload">
                        <source src="$link" type="video/mp4">
                    </video>
                </body>
                </html>
            """.trimIndent()

            wvYoutube.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        } else if (link.isNotEmpty() && link != "null") {
            wvYoutube.visibility = View.GONE
            ivQuestionImage.visibility = View.VISIBLE

            wvYoutube.loadUrl("about:blank")
            Glide.with(this).load(link).into(ivQuestionImage)
        } else {
            ivQuestionImage.visibility = View.GONE
            wvYoutube.visibility = View.GONE
            wvYoutube.loadUrl("about:blank")
        }

        val buttons = listOf(btnA, btnB, btnC, btnD)
        val defaultColor = ColorStateList.valueOf(Color.parseColor("#333333"))

        buttons.forEachIndexed { index, button ->
            button.isEnabled = true
            button.backgroundTintList = defaultColor
            button.text = button.text.toString().replace(" \uD83D\uDC64", "")
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
        val timeSpent = System.currentTimeMillis() - questionStartTime
        questionTimes.add(timeSpent)

        val correctIndex = questions[currentQuestionIndex].correctIndex
        val buttons = listOf(btnA, btnB, btnC, btnD)
        buttons.forEach { it.isEnabled = false }

        val isCorrect = selectedIndex == correctIndex
        if (isCorrect) score++

        if (isCorrect) {
            clickedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            clickedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            if (correctIndex in buttons.indices) {
                buttons[correctIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            }
        }

        proceedToNext(isCorrect, selectedIndex)
    }

    private fun handleTimeOut() {
        questionTimes.add(10000L)
        val correctIndex = questions[currentQuestionIndex].correctIndex
        val buttons = listOf(btnA, btnB, btnC, btnD)

        buttons.forEach { it.isEnabled = false }
        if (correctIndex in buttons.indices) {
            buttons[correctIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        }

        proceedToNext(false, 99)
    }

    private fun proceedToNext(isCorrect: Boolean, selectedIndex: Int) {
        if (roomPin != null) {
            tvQuestion.text = "Oczekiwanie..."
            waitingForOpponentSeconds = 0
            syncWithServer(isCorrect, selectedIndex)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                moveToNextQuestion()
            }, 1500)
        }
    }

    private fun syncWithServer(isCorrect: Boolean, selectedIndex: Int) {
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Anonim") ?: "Anonim"
        val scoreToAdd = if (isCorrect) 1 else 0
        val url = "https://quiz-app.alwaysdata.net/api/sync_question.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val allReady = json.optBoolean("all_ready", false)

                        if (allReady || waitingForOpponentSeconds >= 15) {
                            syncRunnable?.let { syncHandler.removeCallbacks(it) }

                            val oppAnswers = json.optJSONArray("opponent_answers")
                            val correctIndex = questions[currentQuestionIndex].correctIndex
                            val buttons = listOf(btnA, btnB, btnC, btnD)

                            if (oppAnswers != null) {
                                for (i in 0 until oppAnswers.length()) {
                                    val oppAnswer = oppAnswers.getInt(i)
                                    if (oppAnswer in buttons.indices) {
                                        val oppBtn = buttons[oppAnswer]
                                        // Dodaj ikonkę, jeśli jeszcze jej nie ma
                                        if (!oppBtn.text.toString().contains("\uD83D\uDC64")) {
                                            oppBtn.text = oppBtn.text.toString() + " \uD83D\uDC64"
                                        }
                                        if (oppAnswer != correctIndex) {
                                            oppBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
                                        }
                                    }
                                }
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                moveToNextQuestion()
                            }, 2000)

                        } else {
                            syncRunnable = Runnable {
                                waitingForOpponentSeconds++
                                syncWithServer(isCorrect, selectedIndex)
                            }
                            syncHandler.postDelayed(syncRunnable!!, 1000)
                        }
                    }
                } catch (e: JSONException) { e.printStackTrace() }
            },
            {
                syncRunnable = Runnable {
                    waitingForOpponentSeconds++
                    syncWithServer(isCorrect, selectedIndex)
                }
                syncHandler.postDelayed(syncRunnable!!, 1000)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "pin" to roomPin!!,
                    "username" to username,
                    "question_index" to currentQuestionIndex.toString(),
                    "score_to_add" to scoreToAdd.toString(),
                    "answer_index" to selectedIndex.toString()
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun moveToNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            displayQuestion()
        } else {
            showFinalScore()
        }
    }

    private fun showFinalScore() {
        countDownTimer?.cancel()
        syncRunnable?.let { syncHandler.removeCallbacks(it) }

        if (roomPin != null) {
            val intent = Intent(this, MultiplayerScoreActivity::class.java)
            intent.putExtra("ROOM_PIN", roomPin)
            startActivity(intent)
            finish()
        } else {
            val totalTimeMillis = System.currentTimeMillis() - startTimeInMillis
            val totalSeconds = (totalTimeMillis / 1000).toInt()
            val totalFormatted = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)

            val avgTimeSeconds = if (questionTimes.isNotEmpty()) (questionTimes.average() / 1000).toInt() else 0

            setContentView(R.layout.activity_score)

            findViewById<TextView>(R.id.tvScore).text = "Twój wynik: $score / ${questions.size}"
            findViewById<TextView>(R.id.tvPercentage).text = "${(score * 100) / questions.size}%"
            findViewById<TextView>(R.id.tvTime).text = "Całkowity czas: $totalFormatted\nŚredni czas na pytanie: ${avgTimeSeconds}s"

            saveResultToDatabase(score, questions.size)

            findViewById<Button>(R.id.btnReturnMenu).setOnClickListener { finish() }
        }
    }

    private fun saveResultToDatabase(finalScore: Int, maxScore: Int) {
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Anonim") ?: "Anonim"
        val url = "https://quiz-app.alwaysdata.net/api/save_result.php"

        val stringRequest = object : StringRequest(Method.POST, url, {}, {}) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "username" to username,
                    "quiz_id" to quizId.toString(),
                    "score" to finalScore.toString(),
                    "max_score" to maxScore.toString()
                )
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        syncRunnable?.let { syncHandler.removeCallbacks(it) }
    }
}

data class Question(
    val text: String,
    val imageUrl: String?,
    val a1: String,
    val a2: String,
    val a3: String,
    val a4: String,
    val correctIndex: Int
)