package com.example.lockband.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockband.data.room.entities.AppState
import com.example.lockband.data.room.repos.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing data about AppStates in Database and updates of it
 */
@HiltViewModel
class AppListViewModel @Inject internal constructor(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    val appStates = appStateRepository.getAppStates()

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