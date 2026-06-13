package com.selzxrat.v5.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.selzxrat.v5.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val etFirebaseUrl = findViewById<EditText>(R.id.etFirebaseUrl)
        val etRefPath = findViewById<EditText>(R.id.etRefPath)
        val switchDark = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val switchNotif = findViewById<SwitchCompat>(R.id.switchNotifications)
        val switchAutoReconnect = findViewById<SwitchCompat>(R.id.switchAutoReconnect)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)
        val etReconnectInterval = findViewById<EditText>(R.id.etReconnectInterval)
        val btnClearExfil = findViewById<Button>(R.id.btnClearExfiltrated)
        val btnTestConnection = findViewById<Button>(R.id.btnTestConnection)

        btnSave.setOnClickListener {
            val prefs = getSharedPreferences("selzxrat_settings", MODE_PRIVATE)
            prefs.edit().apply {
                putString("firebase_url", etFirebaseUrl.text.toString())
                putString("ref_path", etRefPath.text.toString())
                putBoolean("dark_mode", switchDark.isChecked)
                putBoolean("notifications", switchNotif.isChecked)
                putBoolean("auto_reconnect", switchAutoReconnect.isChecked)
                putInt("reconnect_interval", etReconnectInterval.text.toString().toIntOrNull() ?: 30)
                apply()
            }
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        btnClearExfil.setOnClickListener {
            com.selzxrat.v5.C2Manager.clearExfiltratedData()
            Toast.makeText(this, "Exfiltrated data cleared", Toast.LENGTH_SHORT).show()
        }

        btnTestConnection.setOnClickListener {
            Toast.makeText(this, "Connection test not implemented here", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}