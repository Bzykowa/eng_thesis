package com.example.lockband.utils

import android.content.Context
import com.example.lockband.miband3.model.BatteryInfo
import java.util.*

enum class MiBandServiceState {
    STARTED,
    STOPPED
}

private const val name = "MIBAND_KEY"
private const val addressKey = "MIBAND_ADDRESS"
private const val pairedKey = "MIBAND_PAIRED"
private const val stateKey = "MIBAND_SERVICE_STATE"
private const val batteryLevel = "MIBAND_BATTERY_LEVEL"
private const val batteryCycles = "MIBAND_BATTERY_CYCLES"
private const val batteryStatus = "MIBAND_BATTERY_STATUS"
private const val batterylastCharged = "MIBAND_BATTERY_LAST_CHARGED"
private const val serialNumber = "MIBAND_SERIAL_NUMBER"
private const val hardwareRevision = "MIBAND_HARDWARE_REVISION"
private const val softwareRevision = "MIBAND_SOFTWARE_REVISION"

fun setMiBandAddress(context: Context, address: String) =
    getPreferences(context, name).edit().let {
        it.putString(addressKey, address)
        it.apply()
    }


fun getMiBandPaired(context: Context): Boolean =
    getPreferences(context, name).getBoolean(pairedKey, false)


fun setMiBandPaired(context: Context, paired: Boolean) =
    getPreferences(context, name).edit().let {
        it.putBoolean(pairedKey, paired)
        it.apply()
    }


fun getMiBandAddress(context: Context): String =
    getPreferences(context, name).getString(addressKey, "err")!!


fun setMiBandServiceState(context: Context, state: MiBandServiceState) =
    getPreferences(context, name).edit().let {
        it.putString(stateKey, state.name)
        it.apply()
    }


fun getMiBandServiceState(context: Context): MiBandServiceState = MiBandServiceState.valueOf(
    getPreferences(context, name).getString(
        stateKey,
        MiBandServiceState.STOPPED.name
    )!!
)


fun setMiBandBatteryInfo(context: Context, batteryInfo: BatteryInfo) =
    getPreferences(context, name).edit().let {
        it.putInt(batteryLevel, batteryInfo.level)
        it.putInt(batteryCycles, batteryInfo.cycles)
        it.putString(batteryStatus, batteryInfo.status?.name)
        it.putLong(batterylastCharged, batteryInfo.lastChargedDate.timeInMillis)
        it.apply()
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

fun setMiBandSerialNumber(context: Context, serialNum: String) =
    getPreferences(context, name).edit().let {
        it.putString(serialNumber, serialNum)
        it.apply()
    }


fun getMiBandSerialNumber(context: Context): String =
    getPreferences(context, name).getString(serialNumber, "err")!!

fun setMiBandHardwareRevision(context: Context, hw_rev: String) =
    getPreferences(context, name).edit().let {
        it.putString(hardwareRevision, hw_rev)
        it.apply()
    }

fun getMiBandHardwareRevision(context: Context): String =
    getPreferences(context, name).getString(hardwareRevision, "err")!!

fun setMiBandSoftwareRevision(context: Context, sw_rev: String) =
    getPreferences(context, name).edit().let {
        it.putString(softwareRevision, sw_rev)
        it.apply()
    }

fun getMiBandSoftwareRevision(context: Context): String =
    getPreferences(context, name).getString(softwareRevision, "err")!!

