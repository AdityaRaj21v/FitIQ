package com.example.fitiq

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.*
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var profileIcon: ImageView
    private lateinit var userNameText: TextView
    private lateinit var stepsText: TextView
    private lateinit var calorieText: TextView
    private lateinit var distanceText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var goalText: TextView
    private lateinit var healthScoreValue: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    private var userWeight = 70.0
    private var userHeight = 170.0
    private var userAge = 30
    private var userGender = "Male"
    private var dailyGoal = 10000

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
    private val RC_SIGN_IN = 1000

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        profileIcon = view.findViewById(R.id.profile_icon)
        userNameText = view.findViewById(R.id.user_name_text)
        stepsText = view.findViewById(R.id.step_count)
        calorieText = view.findViewById(R.id.calories)
        distanceText = view.findViewById(R.id.distance)
        progressBar = view.findViewById(R.id.step_progress_bar)
        goalText = view.findViewById(R.id.daily_goal_text)
        healthScoreValue = view.findViewById(R.id.health_score_value)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        sharedPreferences = requireContext().getSharedPreferences("fitPrefs", Context.MODE_PRIVATE)

        fetchUserData()
        fetchDailyGoal()
        startFitnessDataPolling()

        return view
    }

    private fun fetchUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val username = currentUser.displayName ?: "Unknown User"
            val profileImageUrl = currentUser.photoUrl.toString()
            userNameText.text = username

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(profileImageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                    withContext(Dispatchers.Main) {
                        profileIcon.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val uid = currentUser.uid
            database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        userWeight = it.weight
                        userHeight = it.height
                        userAge = it.age
                        userGender = it.gender
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "DB Error: ${error.message}")
                }
            })
        } else {
            userNameText.text = "User not logged in"
        }
    }

    private fun fetchDailyGoal() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        database.child(uid).child("dailyGoal").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dailyGoal = snapshot.getValue(Int::class.java) ?: 10000
                updateGoalText()
            }

            override fun onCancelled(error: DatabaseError) {
                dailyGoal = 10000
                updateGoalText()
            }
        })
    }

    private fun updateGoalText() {
        goalText.text = "Goal: $dailyGoal Steps"
        progressBar.max = dailyGoal
    }

    private fun checkPermissionsAndReadFitnessData(fromPolling: Boolean = false) {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())

        if (account == null) {
            if (!fromPolling) {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
                val signInClient = GoogleSignIn.getClient(requireActivity(), gso)
                startActivityForResult(signInClient.signInIntent, RC_SIGN_IN)
            } else {
                Log.d("FitnessPolling", "Skipping sign-in prompt during background polling.")
            }
            return
        }

        val fitAccount = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
        val hasAskedBefore = sharedPreferences.getBoolean("hasAskedPermissionBefore", false)

        if (!GoogleSignIn.hasPermissions(fitAccount, fitnessOptions)) {
            if (!hasAskedBefore && !fromPolling) {
                sharedPreferences.edit().putBoolean("hasAskedPermissionBefore", true).apply()
                GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    fitAccount,
                    fitnessOptions
                )
            } else {
                Log.d("FitnessPermission", "Permission denied before. Not requesting again.")
            }
        } else {
            readFitnessData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                task.getResult(ApiException::class.java)
                checkPermissionsAndReadFitnessData()
            } catch (e: ApiException) {
                Log.e("SignIn", "Sign-in failed: ${e.statusCode}")
            }
        }

        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
            if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                readFitnessData()
            }
        }
    }

    private fun readFitnessData() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_HEART_POINTS)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { parseFitnessData(it) }
            .addOnFailureListener {
                Log.e("HomeFragment", "Google Fit read failed", it)
                showDefaultValues()
            }
    }

    private fun parseFitnessData(response: DataReadResponse) {
        var totalSteps = 0
        var totalDistance = 0f
        var totalHeartPoints = 0f

        for (bucket in response.buckets) {
            for (dataSet in bucket.dataSets) {
                for (dp in dataSet.dataPoints) {
                    when (dp.dataType.name) {
                        DataType.TYPE_STEP_COUNT_DELTA.name -> {
                            totalSteps += dp.getValue(Field.FIELD_STEPS).asInt()
                        }
                        DataType.TYPE_DISTANCE_DELTA.name -> {
                            totalDistance += dp.getValue(Field.FIELD_DISTANCE).asFloat()
                        }
                        DataType.TYPE_HEART_POINTS.name -> {
                            totalHeartPoints += dp.getValue(Field.FIELD_INTENSITY).asFloat()
                        }
                    }
                }
            }
        }

        updateUI(totalSteps, totalDistance, totalHeartPoints)
    }

    private fun updateUI(steps: Int, distance: Float, heartPoints: Float) {
        activity?.runOnUiThread {
            stepsText.text = "$steps Steps"
            val totalBMR = calculateBMR(userWeight, userHeight, userAge, userGender)
            val totalCaloriesBurned = calculateCaloriesBurned(totalBMR, steps)
            calorieText.text = "${totalCaloriesBurned.toInt()} Cal"
            distanceText.text = "%.2f Km".format(distance / 1000)
            healthScoreValue.text = heartPoints.toInt().toString()
            updateProgressBar(steps)
        }
    }

    private fun updateProgressBar(steps: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, steps)
            .setDuration(1000)
            .start()
    }

    private fun showDefaultValues() {
        activity?.runOnUiThread {
            stepsText.text = "0 Steps"
            calorieText.text = "0 Cal"
            distanceText.text = "0.00 Km"
            progressBar.progress = 0
            healthScoreValue.text = "0"
        }
    }

    private fun calculateBMR(weight: Double, height: Double, age: Int, gender: String): Double {
        return if (gender.equals("Male", ignoreCase = true)) {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        }
    }

    private fun calculateCaloriesBurned(bmr: Double, steps: Int): Double {
        val caloriesPerStep = 0.04
        val activityFactor = when {
            steps < 5000 -> 1.2
            steps < 10000 -> 1.375
            steps < 15000 -> 1.55
            else -> 1.725
        }
        return (bmr * activityFactor / 24) + (steps * caloriesPerStep)
    }

    private fun startFitnessDataPolling() {
        lifecycleScope.launch {
            while (true) {
                checkPermissionsAndReadFitnessData(fromPolling = true)
                delay(10000)
            }
        }
    }
}
