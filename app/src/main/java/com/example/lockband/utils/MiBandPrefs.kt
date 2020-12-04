package com.example.lockband.utils

import android.content.Context
import com.khmelenko.lab.miband.model.BatteryInfo
import java.util.*

enum class MiBandServiceState {
    STARTED,
    STOPPED
}

private const val name = "MIBAND_KEY"
private const val addressKey = "MIBAND_ADDRESS"
private const val stateKey = "MIBAND_SERVICE_STATE"
private const val batteryLevel = "MIBAND_BATTERY_LEVEL"
private const val batteryCycles = "MIBAND_BATTERY_CYCLES"
private const val batteryStatus = "MIBAND_BATTERY_STATUS"
private const val batterylastCharged = "MIBAND_BATTERY_LAST_CHARGED"

fun setMiBandAddress(context: Context, address: String) {
    val sharedPrefs = getPreferences(context, name)
    sharedPrefs.edit().let {
        it.putString(addressKey, address)
        it.apply()
    }
}

fun getMiBandAddress(context: Context): String {
    return getPreferences(context, name).getString(addressKey, "err")!!
}

fun setMiBandServiceState(context: Context, state: MiBandServiceState) {
    val sharedPrefs = getPreferences(context, name)
    sharedPrefs.edit().let {
        it.putString(stateKey, state.name)
        it.apply()
    }
}

fun getMiBandServiceState(context: Context): MiBandServiceState {
    return MiBandServiceState.valueOf(
        getPreferences(context, name).getString(
            stateKey,
            MiBandServiceState.STOPPED.name
        )!!
    )
}

fun setMiBandBatteryInfo(context: Context, batteryInfo: BatteryInfo) {
    val sharedPrefs = getPreferences(context, name)
    sharedPrefs.edit().let {
        it.putInt(batteryLevel, batteryInfo.level)
        it.putInt(batteryCycles, batteryInfo.cycles)
        it.putString(batteryStatus, batteryInfo.status?.name)
        it.putLong(batterylastCharged, batteryInfo.lastChargedDate.timeInMillis)
        it.apply()
    }
}

fun getMiBandBatteryInfo(context: Context): BatteryInfo {
    val sharedPrefs = getPreferences(context, name)
    return BatteryInfo(
        sharedPrefs.getInt(batteryLevel, 0),
        sharedPrefs.getInt(batteryCycles, 0),
        BatteryInfo.Status.valueOf(
            sharedPrefs.getString(batteryStatus, BatteryInfo.Status.UNKNOWN.name)!!
        ),
        Calendar.getInstance().apply { timeInMillis = sharedPrefs.getLong(batterylastCharged, 0) }
    )
}
