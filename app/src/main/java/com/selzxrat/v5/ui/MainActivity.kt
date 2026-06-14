package com.selzxrat.v5.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.selzxrat.v5.R

class MainActivity : AppCompatActivity() {

    // --- PASTE DI SINI ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pasang UI-nya
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
    }
    // ---------------------

}
