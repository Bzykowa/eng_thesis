package com.example.lockband.miband3.model

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
    val UUID_SERVICE_MILI: UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_MIBAND2: UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")

    /**
     * Vibration service (correct)
     */
    val UUID_SERVICE_VIBRATION: UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")

    /**
     * Heart rate service (correct)
     */
    val UUID_SERVICE_HEARTRATE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

    /**
     * Notification service (correct)
     */
    val UUID_SERVICE_NOTIFICATION: UUID = UUID.fromString("00001811-0000-1000-8000-00805f9b34fb")

    /**
     * Device information service (correct)
     */
    val UUID_SERVICE_DEVICE_INFORMATION: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

    /**
     * Unknown services
     */
    val UUID_SERVICE_UNKNOWN1: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_UNKNOWN2: UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")


    // DESCRIPTORS (correct)

    val UUID_DESCRIPTOR_UPDATE_NOTIFICATION: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


    // CHARACTERISTICS

    val UUID_CHAR_SERIAL_NUMBER: UUID = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")

    val UUID_CHAR_HARDWARE_REVISION: UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")

    val UUID_CHAR_SOFTWARE_REVISION: UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")

    /**
     * Notification (correct)
     */
    val UUID_CHAR_NOTIFICATION: UUID = UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")

    /**
     * User info (correct)
     */
    val UUID_CHAR_USER_INFO: UUID = UUID.fromString("00000008-0000-3512-2118-0009af100700")

    /**
     * Used for manipulations with service control (correct)
     */
    val UUID_CHAR_CONTROL_POINT: UUID = UUID.fromString("00000003-0000-3512-2118-0009af100700")

    /**
     * Used for steps measurement (correct)
     */
    val UUID_CHAR_REALTIME_STEPS: UUID = UUID.fromString("00000007-0000-3512-2118-0009af100700")

    /**
     * Used for getting battery info (correct)
     */
    val UUID_CHAR_BATTERY: UUID = UUID.fromString("00000006-0000-3512-2118-0009af100700")

    /**
     * Used for fetching sensor data (correct)
     */
    val UUID_CHAR_SENSOR_DATA: UUID = UUID.fromString("00000001-0000-3512-2118-0009af100700")

    /**
     * Used for pairing device (correct)
     */
    val UUID_CHAR_PAIR: UUID = UUID.fromString("00000009-0000-3512-2118-0009af100700")

    /**
     * Used for enabling/disabling vibration (correct)
     */
    val UUID_CHAR_VIBRATION: UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")

    /**
     * Used for reading heart rate data (correct)
     */
    val UUID_CHAR_HEARTRATE: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val UUID_CONTROL_HEARTRATE: UUID = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")

    /**
     * Used for setting time (correct)
     */
    val UUID_CHAR_DATA_TIME: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")

    /**
     * Used for getting events recorded by band (correct)
     */
    val UUID_CHAR_DEVICEEVENT: UUID = UUID.fromString("00000010-0000-3512-2118-0009af100700")

}
