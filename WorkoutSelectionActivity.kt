package com.example.fitiq


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class WorkoutSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_workout)

        findViewById<ImageView>(R.id.btnCardio).setOnClickListener {
            openWorkout("Cardio")
        }
        findViewById<ImageView>(R.id.btnLegs).setOnClickListener {
            openWorkout("Legs")
        }
        findViewById<ImageView>(R.id.btnChest).setOnClickListener {
            openWorkout("Chest")
        }
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            openWorkout("Back")
        }
        findViewById<ImageView>(R.id.btnShoulders).setOnClickListener {
            openWorkout("Shoulders")
        }
        findViewById<ImageView>(R.id.btnBiceps).setOnClickListener {
            openWorkout("Biceps")
        }
        findViewById<ImageView>(R.id.btnTriceps).setOnClickListener {
            openWorkout("Triceps")
        }
    }

    private fun openWorkout(workoutType: String) {
        val intent = Intent(this, WorkoutExercisesActivity::class.java)
        intent.putExtra("WORKOUT_TYPE", workoutType)
        startActivity(intent)
    }
}

