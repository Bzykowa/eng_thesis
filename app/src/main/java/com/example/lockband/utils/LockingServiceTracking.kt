package com.example.lockband.utils

import android.content.Context
import android.content.SharedPreferences

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val name = "LOCKED_KEY"
private const val key = "LOCKED_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    return ServiceState.valueOf(getPreferences(context).getString(key, ServiceState.STOPPED.name)!!)
}

private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}
