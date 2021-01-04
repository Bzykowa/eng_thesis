package com.example.lockband.utils

import android.content.Context

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val name = "LOCKED_KEY"
private const val key = "LOCKED_STATE"

fun setServiceState(context: Context, state: ServiceState) =
    getPreferences(context, name).edit().let {
        it.putString(key, state.name)
        it.apply()
    }


fun getServiceState(context: Context): ServiceState = ServiceState.valueOf(
    getPreferences(context, name).getString(
        key,
        ServiceState.STOPPED.name
    )!!
)


