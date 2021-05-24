package com.example.lockband.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.lockband.data.room.repos.HeartRateRepository

import com.example.lockband.data.room.repos.StepRepository

/**
 * ViewModel allowing real-time updates of values in EventsStatsFragment
 */
class StatsViewModel @ViewModelInject internal constructor(
    private val stepRepository: StepRepository,
    private val hrRepository: HeartRateRepository
) : ViewModel() {

    private var hr = hrRepository.getLatestHeartRateSample()
    private var phoneSteps = stepRepository.getLatestPhoneStepSampleLive()
    private var bandSteps = stepRepository.getLatestBandStepSampleLive()

    //Transformations applied to get LiveData<String>
    var hrStr = Transformations.map(hr){ hr -> hr.heartRate.toString()}
    var bandStepsStr = Transformations.map(bandSteps){ bandSteps -> bandSteps.stepCount.toString()}
    var phoneStepsStr = Transformations.map(phoneSteps){ phoneSteps -> phoneSteps.stepCount.toString()}
}