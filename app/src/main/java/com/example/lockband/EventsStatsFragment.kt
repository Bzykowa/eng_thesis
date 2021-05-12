package com.example.lockband

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lockband.data.room.repos.HeartRateRepository
import com.example.lockband.data.room.repos.StepRepository
import com.example.lockband.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EventsStatsFragment : Fragment() {

    @Inject
    lateinit var stepRepository: StepRepository

    @Inject
    lateinit var heartRateRepository: HeartRateRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStatsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.stepsBandValue.text = stepRepository.getLatestBandStepSample().stepCount.toString()
        binding.stepsPhoneValue.text = stepRepository.getLatestPhoneStepSample().stepCount.toString()
        binding.hrValue.text = heartRateRepository.getLatestHeartRateSample().heartRate.toString()

        return binding.root
    }
}