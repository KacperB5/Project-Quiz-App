package com.example.mobilequizapp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class WelcomeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val lang = sharedPref.getString("LANG", "pl") ?: "pl"
        super.attachBaseContext(wrapContext(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Gracz") ?: "Gracz"

        val tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        tvWelcomeUser.text = getString(R.string.welcome_user_format, username)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val btnLangPl = findViewById<TextView>(R.id.btnLangPl)
        val btnLangEn = findViewById<TextView>(R.id.btnLangEn)

        btnLangPl.setOnClickListener {
            setAppLocale("pl")
        }

        btnLangEn.setOnClickListener {
            setAppLocale("en")
        }

        bottomNavigation.itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)

        btnLogout.setOnClickListener {
            sharedPref.edit()
                .remove("USERNAME")
                .remove("ROLE")
                .apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        if (savedInstanceState == null) {
            loadFragment(QuizzesFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_quizzes -> QuizzesFragment()
                R.id.nav_ranking -> RankingFragment()
                R.id.nav_stats -> StatsFragment()
                R.id.nav_about -> AboutFragment()
                else -> QuizzesFragment()
            }
            loadFragment(selectedFragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setAppLocale(languageCode: String) {
        val sharedPref = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE)
        sharedPref.edit().putString("LANG", languageCode).apply()

        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)

        val toastMsg = if (languageCode == "pl") "Zmieniono język na Polski! 🇵🇱" else "Language switched to English! 🇬🇧"
        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()

        recreate()
    }

    companion object {
        fun wrapContext(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = Configuration(resources.configuration)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = android.os.LocaleList(locale)
                android.os.LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                context.createConfigurationContext(configuration)
            } else {
                configuration.locale = locale
                resources.updateConfiguration(configuration, resources.displayMetrics)
                context
            }
        }
    }
}