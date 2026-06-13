package com.selzxrat.v5.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.selzxrat.v5.R
import com.selzxrat.v5.SELZXRATApp
import com.selzxrat.v5.ui.MainActivity
import org.json.JSONObject

class SELZXFCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SELZXFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Store token for C2
        getSharedPreferences("selzxrat_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received: ${message.data}")

        val data = message.data
        val commandType = data["type"] ?: return
        val payload = data["payload"] ?: ""
        val from = data["from"] ?: "unknown"

        // Show notification
        showNotification(commandType, payload, from)

        // Process command locally
        processCommand(commandType, payload)
    }

    private fun showNotification(type: String, payload: String, from: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, SELZXRATApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("SELZXRAT Command: $type")
            .setContentText("From: $from | $payload")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun processCommand(type: String, payload: String) {
        when (type) {
            "ping" -> Log.d(TAG, "Ping received")
            "alert" -> {
                // Forward to main activity
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("show_alert", payload)
                }
                startActivity(intent)
            }
            else -> Log.d(TAG, "Unknown FCM command: $type")
        }
    }
}