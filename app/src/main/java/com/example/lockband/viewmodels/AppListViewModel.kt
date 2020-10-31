package com.example.lockband.viewmodels

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockband.data.AppState
import com.example.lockband.data.AppStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppListViewModel @ViewModelInject internal constructor(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    val appStates = appStateRepository.getAppStates()

    fun upsertAppState(packageName: String, label: String, locked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appStateRepository.upsertAppState(AppState(packageName, label, locked))
        }
    }

    fun insertAppState(packageName: String, label: String, locked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appStateRepository.insertAppState(AppState(packageName, label, locked))
        }
    }

    fun updateAppState(app : AppState) {
        viewModelScope.launch(Dispatchers.IO) {
            appStateRepository.updateAppState(app)
        }
    }

}