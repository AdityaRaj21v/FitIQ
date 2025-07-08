package com.example.fitiq

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var typewriterText: TextView

    private val typeText = "Be fit with FitIQ"
    private var index = 0
    private val delay: Long = 100

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImage = findViewById(R.id.logoImage)
        typewriterText = findViewById(R.id.typewriterText)

        // Popping animation for Logo
        logoImage.visibility = View.VISIBLE
        val scale = AnimationUtils.loadAnimation(this, R.anim.pop)
        logoImage.startAnimation(scale)

        // Start typewriter after popping animation
        scale.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                typewriterText.visibility = View.VISIBLE
                typeTextWithDelay()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        auth = FirebaseAuth.getInstance()
    }

    private fun typeTextWithDelay() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (index < typeText.length) {
                    typewriterText.text = typewriterText.text.toString() + typeText[index]
                    index++
                    handler.postDelayed(this, delay)
                } else {
                    handler.postDelayed({
                        proceedAfterSplash()
                    }, 1000)
                }
            }
        }, delay)
    }

    private fun proceedAfterSplash() {
        val user = auth.currentUser
        if (user != null) {
            val userRef = database.getReference("users").child(user.uid)
            userRef.get().addOnSuccessListener {
                if (it.exists() && it.hasChild("dailyGoal") && it.hasChild("gender") && it.hasChild("age")) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, UserDetailsActivity::class.java))
                }
                finish()
            }.addOnFailureListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
