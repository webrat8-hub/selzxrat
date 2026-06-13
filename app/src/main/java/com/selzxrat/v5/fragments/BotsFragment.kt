package com.selzxrat.v5.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R
import com.selzxrat.v5.adapters.BotAdapter
import com.selzxrat.v5.ui.BotDetailActivity

class BotsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: BotAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_bots, container, false)

        recyclerView = view.findViewById(R.id.recyclerBots)
        swipeRefresh = view.findViewById(R.id.swipeRefreshBots)

        adapter = BotAdapter { deviceId ->
            val intent = Intent(requireContext(), BotDetailActivity::class.java)
            intent.putExtra("device_id", deviceId)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener { refreshBots() }

        C2Manager.onBotUpdate { id, bot ->
            requireActivity().runOnUiThread {
                adapter.updateBot(id, bot)
            }
        }
        C2Manager.onBotDisconnected { id ->
            requireActivity().runOnUiThread {
                adapter.removeBot(id)
            }
        }

        refreshBots()
        return view
    }

    private fun refreshBots() {
        C2Manager.getBots { bots ->
            requireActivity().runOnUiThread {
                adapter.setBots(bots)
                swipeRefresh.isRefreshing = false
            }
        }
    }
}
