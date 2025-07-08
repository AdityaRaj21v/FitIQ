package com.example.fitiq

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Sync bottom nav when swiping
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.getItem(position).isChecked = true
            }
        })

        // Handle navigation item selection
        bottomNav.setOnItemSelectedListener { item ->
            val anim = AnimationUtils.loadAnimation(this, R.anim.nav_icon_click)
            val view = bottomNav.findViewById<android.view.View>(item.itemId)
            view?.startAnimation(anim)

            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_workout -> viewPager.currentItem = 1
                R.id.nav_progress -> viewPager.currentItem = 2
                R.id.nav_profile -> viewPager.currentItem = 3
            }
            true
        }
    }
}
