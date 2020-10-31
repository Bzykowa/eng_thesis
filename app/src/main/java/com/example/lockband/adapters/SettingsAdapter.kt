package com.example.lockband.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lockband.HomeViewPagerFragment
import com.example.lockband.HomeViewPagerFragmentDirections
import com.example.lockband.SettingsFragmentDirections
import com.example.lockband.data.AppState
import com.example.lockband.data.SettingsItem
import com.example.lockband.databinding.SettingsItemBinding

class SettingsAdapter : ListAdapter<SettingsItem, RecyclerView.ViewHolder>(SettingsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SettingsViewHolder(
            SettingsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val setting = getItem(position)
        (holder as SettingsViewHolder).bind(setting)
    }

    inner class SettingsViewHolder(private val binding: SettingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.setting?.let { setting ->
                    navigateToSetting(setting, it)
                }
            }
        }

        private fun navigateToSetting(setting : SettingsItem, view : View){
            val direction = when(setting.navId) {
                3 -> SettingsFragmentDirections.actionSettingsFragmentToAppListFragment()
                else -> HomeViewPagerFragmentDirections.actionHomeViewPagerFragmentToSettingsFragment()
            }
            view.findNavController().navigate(direction)
        }

        fun bind(item: SettingsItem) {
            binding.apply {
                setting = item
                executePendingBindings()
            }
        }
    }
}

private class SettingsDiffCallback : DiffUtil.ItemCallback<SettingsItem>() {
    override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem == newItem
    }
}