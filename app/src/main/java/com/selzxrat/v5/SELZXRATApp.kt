package com.selzxrat.v5

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp

class SELZXRATApp : Application() {

    companion object {
        const val CHANNEL_ID = "selzxrat_c2_channel"
        const val CHANNEL_NAME = "SELZXRAT C2 Commands"
        lateinit var instance: SELZXRATApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            // Inisialisasi Firebase tanpa Persistence dulu
            // (Persistence bikin crash kalau service lain akses DB duluan)
            FirebaseApp.initializeApp(this)
            
            // Create notification channel
            createNotificationChannel()
        } catch (e: Exception) {
            // Kalau ini error, berarti masalahnya di library Firebase/Google Services
            Log.e("DEBUG_ERROR", "Gagal inisialisasi: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for C2"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
