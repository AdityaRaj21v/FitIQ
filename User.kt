package com.example.fitiq

data class User(
    val age: Int = 0,
    val gender: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val dailyGoal: Int = 0,
    val activityLevel: String = "",
    val email: String = "",
    val name: String = "",
    val steps: Int = 0
)
