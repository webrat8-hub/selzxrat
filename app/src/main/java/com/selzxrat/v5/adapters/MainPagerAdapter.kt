package com.selzxrat.v5.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.selzxrat.v5.fragments.*

class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount() = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DashboardFragment()
            1 -> BotsFragment()
            2 -> CommandsFragment()
            3 -> ExfiltratedFragment()
            4 -> TerminalFragment()
            else -> DashboardFragment()
        }
    }
}