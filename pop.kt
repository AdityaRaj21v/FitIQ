package com.example.fitiq

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class pop : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Replace with your layout

        val popOutAnim = AnimationUtils.loadAnimation(this, R.anim.text_pop_zoom)

        val headers = listOf(
            R.id.cardioText,
            R.id.legsText,
            R.id.chestText,
            R.id.backText,
            R.id.shouldersText,
            R.id.bicepsText,
            R.id.tricepsText
        )

        for (id in headers) {
            findViewById<TextView>(id).setOnClickListener {
                it.startAnimation(popOutAnim)
            }
        }
    }
}
