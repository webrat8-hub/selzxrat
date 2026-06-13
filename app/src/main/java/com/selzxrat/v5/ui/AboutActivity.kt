package com.selzxrat.v5.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.selzxrat.v5.BuildConfig
import com.selzxrat.v5.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.title = "About SELZXRAT"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<TextView>(R.id.tvVersion).text = "Version ${BuildConfig.VERSION_NAME}"
        findViewById<TextView>(R.id.tvBuild).text = "Build ${BuildConfig.VERSION_CODE}"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
