package com.example.lockband.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lockband.AppListFragment
import com.example.lockband.BandStateFragment
import com.example.lockband.EventsStatsFragment
import com.example.lockband.SettingsFragment
import com.example.lockband.utils.*

class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        BAND_PAGE_INDEX to { BandStateFragment() },
        STATS_PAGE_INDEX to { EventsStatsFragment() },
        SETTINGS_PAGE_INDEX to { SettingsFragment() },
        BLOCKED_APPS_LIST_PAGE_INDEX to { AppListFragment() },
        CHANGE_PASS_PAGE_INDEX to { SettingsFragment() },
        ABOUT_PAGE_INDEX to { SettingsFragment() }
    )

    override fun getItemCount(): Int = fragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return fragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}