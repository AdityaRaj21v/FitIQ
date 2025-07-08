package com.example.fitiq



import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WorkoutExercisesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_exercises)

        val workoutType = intent.getStringExtra("WORKOUT_TYPE")
        val titleTextView = findViewById<TextView>(R.id.workoutTitle)
        val exercisesTextView = findViewById<TextView>(R.id.exercisesList)

        titleTextView.text = "$workoutType Exercises"

        exercisesTextView.text = when (workoutType) {
            "Cardio" -> "Running\nCycling\nJump Rope\nSwimming\nBurpees"
            "Legs" -> "Squats\nLunges\nDeadlifts\nLeg Press\nCalf Raises"
            "Chest" -> "Push Ups\nBench Press\nChest Fly\nIncline Press"
            "Back" -> "Pull Ups\nDeadlifts\nBarbell Rows\nLat Pulldown"
            "Shoulders" -> "Shoulder Press\nLateral Raises\nFront Raises\nArnold Press"
            "Biceps" -> "Bicep Curls\nHammer Curls\nConcentration Curls"
            "Triceps" -> "Tricep Dips\nOverhead Extensions\nClose Grip Bench Press"
            else -> "No exercises found."
        }
    }
}
