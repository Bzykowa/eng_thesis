package com.example.lockband.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.lockband.data.room.repos.HeartRateRepository

import com.example.lockband.data.room.repos.StepRepository

class StatsViewModel @ViewModelInject internal constructor(
    private val stepRepository: StepRepository,
    private val hrRepository: HeartRateRepository
) : ViewModel() {

    private var hr = hrRepository.getLatestHeartRateSample()
    private var phoneSteps = stepRepository.getLatestPhoneStepSample()
    private var bandSteps = stepRepository.getLatestBandStepSample()
    var hrStr = Transformations.map(hr){ hr -> hr.heartRate.toString()}
    var bandStepsStr = Transformations.map(bandSteps){ bandSteps -> bandSteps.stepCount.toString()}
    var phoneStepsStr = Transformations.map(phoneSteps){ phoneSteps -> phoneSteps.stepCount.toString()}

    fun updateValues() {
        hr = hrRepository.getLatestHeartRateSample()
        phoneSteps = stepRepository.getLatestPhoneStepSample()
        bandSteps = stepRepository.getLatestBandStepSample()
        hrStr = Transformations.map(hr){ hr -> hr.heartRate.toString()}
        bandStepsStr = Transformations.map(bandSteps){ bandSteps -> bandSteps.stepCount.toString()}
        phoneStepsStr = Transformations.map(phoneSteps){ phoneSteps -> phoneSteps.stepCount.toString()}
    }
}