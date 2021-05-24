package com.example.lockband.utils

import android.content.Context

/**
 * State of LockingService
 */
enum class LockingServiceState {
    STARTED,
    STOPPED,
}

private const val name = "LOCKED_KEY"
private const val key = "LOCKED_STATE"

/**
 * Sets state of LockingService in SharedPreferences
 *
 * @param context Context needed for accessing SharedPreferences
 * @param state New state of LockingService
 */
fun setLockingServiceState(context: Context, state: LockingServiceState) =
    getPreferences(context, name).edit().let {
        it.putString(key, state.name)
        it.apply()
    }

/**
 * Returns current state of LockingService stored in SharedPreferences
 *
 * @param context Context needed for accessing SharedPreferences
 * @return Current state of LockingService
 */
fun getLockingServiceState(context: Context): LockingServiceState = LockingServiceState.valueOf(
    getPreferences(context, name).getString(
        key,
        LockingServiceState.STOPPED.name
    )!!
)


