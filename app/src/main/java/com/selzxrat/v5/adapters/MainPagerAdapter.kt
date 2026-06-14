package com.selzxrat.v5.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount() = 5

    override fun createFragment(position: Int): Fragment {
        // Return fragment kosong saja untuk tes
        return Fragment() 
    }
}
