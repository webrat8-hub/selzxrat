package com.selzxrat.v5

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.io.File

class SELZXRATApp : Application() {

    companion object {
        const val CHANNEL_ID = "selzxrat_c2_channel"
        const val CHANNEL_NAME = "SELZXRAT C2 Commands"
        lateinit var instance: SELZXRATApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // --- BLACK BOX CRASH LOGGER ---
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            try {
                val errorLog = throwable.stackTraceToString()
                val file = File(getExternalFilesDir(null), "DEBUG_CRASH.txt")
                file.writeText(errorLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            System.exit(1)
        }
        // ------------------------------

        instance = this
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for receiving C2 commands"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
