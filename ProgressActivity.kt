package com.example.fitiq

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProgressActivity : AppCompatActivity() {

    private lateinit var circularProgressBar: ProgressBar
    private lateinit var progressPercent: TextView
    private lateinit var workoutTimeValue: TextView
    private lateinit var caloriesBurnedValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_progress)

        circularProgressBar = findViewById(R.id.circularProgressBar)
        progressPercent = findViewById(R.id.progressPercent)
        workoutTimeValue = findViewById(R.id.workoutTimeValue)
        caloriesBurnedValue = findViewById(R.id.caloriesBurnedValue)

        loadProgressData()
    }

    private fun loadProgressData() {
        // Example data -- You can fetch these from Firebase, Google Fit, etc.
        val progress = 72 // in percentage
        val workoutTime = 45 // in minutes
        val caloriesBurned = 320 // in kcal

        circularProgressBar.progress = progress
        progressPercent.text = "$progress%"

        workoutTimeValue.text = "$workoutTime min"
        caloriesBurnedValue.text = "$caloriesBurned kcal"
    }
}
