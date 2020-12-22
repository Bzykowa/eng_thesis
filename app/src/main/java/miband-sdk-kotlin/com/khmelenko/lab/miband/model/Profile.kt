package com.khmelenko.lab.miband.model

import java.util.*

/**
 * Defines keys for services, descriptors and characteristics
 *
 * @author Dmytro Khmelenko
 */
object Profile {

    // SERVICES

    /**
     * Data service (correct)
     */
    val UUID_SERVICE_MILI = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_MIBAND2 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")

    /**
     * Vibration service (correct)
     */
    val UUID_SERVICE_VIBRATION = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")

    /**
     * Heart rate service (correct)
     */
    val UUID_SERVICE_HEARTRATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

    /**
     * Notification service (correct)
     */
    val UUID_SERVICE_NOTIFICATION = UUID.fromString("00001811-0000-1000-8000-00805f9b34fb")

    /**
     * Unknown services
     */
    val UUID_SERVICE_UNKNOWN1 = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_UNKNOWN2 = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_UNKNOWN4 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_UNKNOWN5 = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb")


    // DESCRIPTORS (correct)

    val UUID_DESCRIPTOR_UPDATE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val UUID_NOTIFICATION_HEARTRATE = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")


    // CHARACTERISTICS

    val UUID_CHAR_DEVICE_INFO = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")

    val UUID_CHAR_DEVICE_NAME = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")

    /**
     * Notification (correct)
     */
    val UUID_CHAR_NOTIFICATION = UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")

    /**
     * User info (bad)
     */
    val UUID_CHAR_USER_INFO = UUID.fromString("0000ff04-0000-1000-8000-00805f9b34fb")

    /**
     * Used for manipulations with service control (bad)
     */
    val UUID_CHAR_CONTROL_POINT = UUID.fromString("0000ff05-0000-1000-8000-00805f9b34fb")

    /**
     * Used for steps measurement (correct)
     */
    val UUID_CHAR_REALTIME_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700")

    /**
     * Used for getting battery info (correct)
     */
    val UUID_CHAR_BATTERY = UUID.fromString("00000006-0000-3512-2118-0009af100700")

    /**
     * Used for fetching sensor data (correct)
     */
    val UUID_CHAR_SENSOR_DATA = UUID.fromString("00000001-0000-3512-2118-0009af100700")

    /**
     * Used for pairing device (correct)
     */
    val UUID_CHAR_PAIR = UUID.fromString("00000009-0000-3512-2118-0009af100700")

    /**
     * Used for enabling/disabling vibration (correct)
     */
    val UUID_CHAR_VIBRATION = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")

    /**
     * Used for reading heart rate data (correct)
     */
    val UUID_CHAR_HEARTRATE = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    /**
     * Used for setting time (correct)
     */
    val UUID_CHAR_DATA_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")


    //bad
    val UUID_CHAR_ACTIVITY = UUID.fromString("0000ff07-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_FIRMWARE_DATA = UUID.fromString("0000ff08-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_LE_PARAMS = UUID.fromString("0000ff09-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_STATISTICS = UUID.fromString("0000ff0b-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_TEST = UUID.fromString("0000ff0d-0000-1000-8000-00805f9b34fb")

}
