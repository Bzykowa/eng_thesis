package com.example.lockband

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lockband.adapters.PagerAdapter
import com.example.lockband.adapters.SettingsAdapter
import com.example.lockband.databinding.FragmentSettingsBinding
import com.example.lockband.utils.SETTINGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val adapter = SettingsAdapter()
        binding.settingsList.adapter = adapter
        adapter.submitList(SETTINGS)

        return binding.root
    }
}