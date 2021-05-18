package com.example.lockband

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lockband.data.MiBandServiceActions
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.databinding.FragmentBandStateBinding
import com.example.lockband.services.LockingService
import com.example.lockband.utils.getMiBandBatteryInfo
import com.example.lockband.utils.getMiBandHardwareRevision
import com.example.lockband.utils.getMiBandSerialNumber
import com.example.lockband.utils.getMiBandSoftwareRevision
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BandStateFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Intent().also { intent ->
            intent.action = MiBandServiceActions.BATTERY.name
            activity?.sendBroadcast(intent)
        }

        val binding = FragmentBandStateBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Intent(requireContext(), LockingService::class.java).also {
                    it.action = LockingServiceActions.START.name
                    requireActivity().startForegroundService(it)
                }
            } else {
                Intent(requireContext(), LockingService::class.java).also {
                    it.action = LockingServiceActions.STOP.name
                    requireActivity().startForegroundService(it)
                }
            }

        }

        binding.serialNum.text = getMiBandSerialNumber(requireContext())
        binding.hardwareRev.text = getMiBandHardwareRevision(requireContext())
        binding.softwareRev.text = getMiBandSoftwareRevision(requireContext())
        binding.batteryPercentage.text = getString(R.string.percent_placeholder, getMiBandBatteryInfo(requireContext()).level)

        return binding.root
    }
}