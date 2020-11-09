package com.example.lockband

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lockband.data.Actions
import com.example.lockband.databinding.FragmentBandStateBinding
import com.example.lockband.services.LockingService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BandStateFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentBandStateBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Intent(requireContext(), LockingService::class.java).also {
                    it.action = Actions.START.name
                    requireActivity().startForegroundService(it)
                }
            } else {
                Intent(requireContext(), LockingService::class.java).also {
                    it.action = Actions.STOP.name
                    requireActivity().startForegroundService(it)
                }
            }

        }

        return binding.root
    }
}