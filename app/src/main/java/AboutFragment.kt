package com.example.mobilequizapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class AboutFragment : Fragment() {

    private data class TutorialStep(
        val title: String,
        val description: String,
        val iconResId: Int
    )

    private lateinit var tutorialSteps: List<TutorialStep>
    private var currentStep = 0

    private lateinit var ivTutorialIcon: ImageView
    private lateinit var tvTutorialTitle: TextView
    private lateinit var tvTutorialDescription: TextView
    private lateinit var tvTutorialPages: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        ivTutorialIcon = view.findViewById(R.id.ivTutorialIcon)
        tvTutorialTitle = view.findViewById(R.id.tvTutorialTitle)
        tvTutorialDescription = view.findViewById(R.id.tvTutorialDescription)
        tvTutorialPages = view.findViewById(R.id.tvTutorialPages)
        btnPrev = view.findViewById(R.id.btnPrevTutorial)
        btnNext = view.findViewById(R.id.btnNextTutorial)

        setupTutorialSteps()

        updateTutorialUI(animate = false)

        btnPrev.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                updateTutorialUI(animate = true)
            }
        }

        btnNext.setOnClickListener {
            if (currentStep < tutorialSteps.size - 1) {
                currentStep++
                updateTutorialUI(animate = true)
            }
        }

        return view
    }

    private fun setupTutorialSteps() {
        tutorialSteps = listOf(
            TutorialStep(
                getString(R.string.tutorial_step1_title),
                getString(R.string.tutorial_step1_desc),
                android.R.drawable.ic_dialog_info
            ),
            TutorialStep(
                getString(R.string.tutorial_step2_title),
                getString(R.string.tutorial_step2_desc),
                android.R.drawable.ic_menu_compass
            ),
            TutorialStep(
                getString(R.string.tutorial_step3_title),
                getString(R.string.tutorial_step3_desc),
                android.R.drawable.ic_menu_share
            ),
            TutorialStep(
                getString(R.string.tutorial_step4_title),
                getString(R.string.tutorial_step4_desc),
                android.R.drawable.ic_menu_myplaces
            )
        )
    }

    /**
     * Aktualizuje widoki oraz dynamicznie formatuje licznik kroków
     * i etykiety przycisków na podstawie aktywnego języka.
     */
    private fun updateTutorialUI(animate: Boolean) {
        if (currentStep !in tutorialSteps.indices) return
        val step = tutorialSteps[currentStep]

        if (animate) {
            val fadeIn = AlphaAnimation(0.3f, 1.0f).apply {
                duration = 300
            }
            ivTutorialIcon.startAnimation(fadeIn)
            tvTutorialTitle.startAnimation(fadeIn)
            tvTutorialDescription.startAnimation(fadeIn)
        }

        ivTutorialIcon.setImageResource(step.iconResId)
        tvTutorialTitle.text = step.title
        tvTutorialDescription.text = step.description

        tvTutorialPages.text = getString(R.string.tutorial_step_counter, currentStep + 1, tutorialSteps.size)

        btnPrev.isEnabled = currentStep > 0
        btnPrev.alpha = if (currentStep > 0) 1.0f else 0.4f

        if (currentStep == tutorialSteps.size - 1) {
            btnNext.text = getString(R.string.btn_finish)
            btnNext.setTextColor(android.graphics.Color.parseColor("#888888"))
        } else {
            btnNext.text = getString(R.string.btn_next)
            btnNext.setTextColor(android.graphics.Color.parseColor("#03DAC5"))
        }
    }
}