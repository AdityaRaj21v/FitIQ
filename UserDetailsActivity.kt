package com.example.fitiq

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var dobInput: TextView
    private lateinit var genderSpinner: Spinner
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var weightInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var goalInput: EditText
    private lateinit var saveButton: Button

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private var selectedAge = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        dobInput = findViewById(R.id.dobInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        activityLevelSpinner = findViewById(R.id.activityLevelSpinner)
        weightInput = findViewById(R.id.weightInput)
        heightInput = findViewById(R.id.heightInput)
        goalInput = findViewById(R.id.stepGoalInput)
        saveButton = findViewById(R.id.submitButton)

        auth = FirebaseAuth.getInstance()

        val genderOptions = arrayOf("Male", "Female", "Other")
        genderSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)

        val activityLevels = arrayOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active")
        activityLevelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, activityLevels)

        dobInput.setOnClickListener {
            showDatePicker()
        }

        saveButton.setOnClickListener {
            val user = auth.currentUser
            if (user != null && selectedAge > 0) {
                val gender = genderSpinner.selectedItem?.toString() ?: ""
                val activityLevel = activityLevelSpinner.selectedItem?.toString() ?: ""
                val weight = weightInput.text.toString().toIntOrNull() ?: 0
                val height = heightInput.text.toString().toIntOrNull() ?: 0
                val goal = goalInput.text.toString().toIntOrNull() ?: 0

                if (weight == 0 || height == 0 || goal == 0) {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val userData = mapOf(
                    "age" to selectedAge,
                    "gender" to gender,
                    "weight" to weight,
                    "height" to height,
                    "dailyGoal" to goal,
                    "activityLevel" to activityLevel,
                    "email" to (user.email ?: ""),
                    "name" to (user.displayName ?: ""),
                    "steps" to 0
                )

                val userRef = database.getReference("users").child(user.uid)
                userRef.updateChildren(userData).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to save details", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select your Date of Birth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, _, _ ->
            selectedAge = year - selectedYear
            dobInput.text = "Age: $selectedAge"
        }, year, month, day)

        datePicker.show()
    }
}
