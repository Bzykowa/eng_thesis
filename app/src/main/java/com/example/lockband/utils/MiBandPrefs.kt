package com.example.lockband.utils

import android.content.Context
import com.example.lockband.miband3.model.BatteryInfo
import java.util.*

/**
 * State of MiBandService
 */
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
private const val disconnectKey = "DISCONNECTS_KEY"

/**
 * Checks if MiBand is paired
 *
 * @param context Context for accessing SharedPreferences
 * @return True if it is, False if it isn't
 */
fun getMiBandPaired(context: Context): Boolean =
    getPreferences(context, name).getBoolean(pairedKey, false)

/**
 * Remembers state of pairing
 *
 * @param context Context for accessing SharedPreferences
 * @param paired Result of pairing
 */
fun setMiBandPaired(context: Context, paired: Boolean) =
    getPreferences(context, name).edit().let {
        it.putBoolean(pairedKey, paired)
        it.apply()
    }

/**
 * Stores MiBand MAC address in SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @param address MAC address of the device
 */
fun setMiBandAddress(context: Context, address: String) =
    getPreferences(context, name).edit().let {
        it.putString(addressKey, address)
        it.apply()
    }

/**
 * Gets MiBand MAC address stored in SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @return MAC address of the device or default value
 */
fun getMiBandAddress(context: Context): String =
    getPreferences(context, name).getString(addressKey, "err")!!

/**
 * Sets state of MiBandService
 *
 * @param context Context for accessing SharedPreferences
 * @param state Current state of MiBandService
 */
fun setMiBandServiceState(context: Context, state: MiBandServiceState) =
    getPreferences(context, name).edit().let {
        it.putString(stateKey, state.name)
        it.apply()
    }

/**
 * Gets MiBandService state stored in SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @return State of MiBandService
 */
fun getMiBandServiceState(context: Context): MiBandServiceState = MiBandServiceState.valueOf(
    getPreferences(context, name).getString(
        stateKey,
        MiBandServiceState.STOPPED.name
    )!!
)

/**
 * Sets current MiBand's battery status
 *
 * @param context Context for accessing SharedPreferences
 * @param batteryInfo Battery information to serialize
 */
fun setMiBandBatteryInfo(context: Context, batteryInfo: BatteryInfo) =
    getPreferences(context, name).edit().let {
        it.putInt(batteryLevel, batteryInfo.level)
        it.putInt(batteryCycles, batteryInfo.cycles)
        it.putString(batteryStatus, batteryInfo.status?.name)
        it.putLong(batterylastCharged, batteryInfo.lastChargedDate.timeInMillis)
        it.apply()
    }

/**
 * Gets current state of MiBand's battery from SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @return Merged information about battery or default placeholder
 */
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

/**
 * Sets serial number of MiBand
 *
 * @param context Context for accessing SharedPreferences
 * @param serialNum Serial number
 */
fun setMiBandSerialNumber(context: Context, serialNum: String) =
    getPreferences(context, name).edit().let {
        it.putString(serialNumber, serialNum)
        it.apply()
    }

/**
 * Gets serial number of MiBand from SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @return Serial number or default value
 */
fun getMiBandSerialNumber(context: Context): String =
    getPreferences(context, name).getString(serialNumber, "unknown")!!

/**
 * Sets hardware version of MiBand
 *
 * @param context Context for accessing SharedPreferences
 * @param hw_rev Hardware version
 */
fun setMiBandHardwareRevision(context: Context, hw_rev: String) =
    getPreferences(context, name).edit().let {
        it.putString(hardwareRevision, hw_rev)
        it.apply()
    }

/**
 * Gets hardware version of MiBand from SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @return Hardware version or default value
 */
fun getMiBandHardwareRevision(context: Context): String =
    getPreferences(context, name).getString(hardwareRevision, "unknown")!!

/**
 * Sets software version of MiBand
 *
 * @param context Context for accessing SharedPreferences
 * @param sw_rev Software version
 */
fun setMiBandSoftwareRevision(context: Context, sw_rev: String) =
    getPreferences(context, name).edit().let {
        it.putString(softwareRevision, sw_rev)
        it.apply()
    }

/**
 * Gets software version of MiBand from SharedPreferences
 *
 * @param context Context for accessing SharedPreferences
 * @return Software version or default value
 */
fun getMiBandSoftwareRevision(context: Context): String =
    getPreferences(context, name).getString(softwareRevision, "unknown")!!

/**
 * Sets consequent number of failed reconnection attempts
 *
 * @param context Context for accessing SharedPreferences
 * @param num Number of failed attempts
 */
fun setDisconnectsNumber(context: Context, num: Int) = getPreferences(context, name).edit().let {
    it.putInt(disconnectKey, num)
    it.apply()
}

/**
 * Gets consequent number of failed reconnection attempts to MiBand
 *
 * @param context Context for accessing SharedPreferences
 * @return Number of failed attempts or default value
 */
fun getDisconnectsNumber(context: Context) = getPreferences(context, name).getInt(disconnectKey, 0)

