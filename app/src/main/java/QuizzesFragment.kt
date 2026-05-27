package com.example.mobilequizapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class QuizzesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quizzes, container, false)

        val btnSingleplayer = view.findViewById<Button>(R.id.btnSingleplayer)
        val btnMultiPlayer = view.findViewById<Button>(R.id.btnMultiPlayer)

        btnSingleplayer.setOnClickListener {
            startActivity(Intent(activity, MenuActivity::class.java))
        }

        btnMultiPlayer.setOnClickListener {
            startActivity(Intent(activity, MultiplayerActivity::class.java))
        }

        return view
    }
}