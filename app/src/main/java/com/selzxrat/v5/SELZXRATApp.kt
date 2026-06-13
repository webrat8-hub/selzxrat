package com.selzxrat.v5

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SELZXRATApp : Application() {

    companion object {
        const val CHANNEL_ID = "selzxrat_c2_channel"
        const val CHANNEL_NAME = "SELZXRAT C2 Commands"
        lateinit var instance: SELZXRATApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Enable Firebase offline persistence for C2 resilience
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Create notification channel
        createNotificationChannel()

        // Initialize Firebase
        FirebaseStorage.getInstance()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for receiving C2 commands"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}