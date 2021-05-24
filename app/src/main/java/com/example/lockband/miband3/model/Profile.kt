package com.example.lockband.miband3.model

import java.util.*

/**
 * Defines keys for services, descriptors and characteristics
 */
object Profile {

    /**
     * SERVICES
     */

    /**
     * Device specific services
     */
    val UUID_SERVICE_MILI: UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_MIBAND2: UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")

    /**
     * Heart rate service
     */
    val UUID_SERVICE_HEARTRATE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

    /**
     * Device information service
     */
    val UUID_SERVICE_DEVICE_INFORMATION: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")


    /**
     * DESCRIPTORS
     */

    val UUID_DESCRIPTOR_UPDATE_NOTIFICATION: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


    /**
     * CHARACTERISTICS
     */


    /**
     * UUID_SERVICE_DEVICE_INFORMATION
     */
    val UUID_CHAR_SERIAL_NUMBER: UUID = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")

    val UUID_CHAR_HARDWARE_REVISION: UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")

    val UUID_CHAR_SOFTWARE_REVISION: UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")

    /**
     * User info (UUID_SERVICE_MILI)
     */
    val UUID_CHAR_USER_INFO: UUID = UUID.fromString("00000008-0000-3512-2118-0009af100700")

    /**
     * Used for manipulations with service control (UUID_SERVICE_MILI)
     */
    val UUID_CHAR_CONTROL_POINT: UUID = UUID.fromString("00000003-0000-3512-2118-0009af100700")

    /**
     * Used for steps measurement (UUID_SERVICE_MILI)
     */
    val UUID_CHAR_REALTIME_STEPS: UUID = UUID.fromString("00000007-0000-3512-2118-0009af100700")

    /**
     * Used for getting battery info (UUID_SERVICE_MILI)
     */
    val UUID_CHAR_BATTERY: UUID = UUID.fromString("00000006-0000-3512-2118-0009af100700")


    /**
     * Used for pairing device (UUID_SERVICE_MIBAND2)
     */
    val UUID_CHAR_PAIR: UUID = UUID.fromString("00000009-0000-3512-2118-0009af100700")

    /**
     * Used for reading heart rate data (UUID_SERVICE_HEARTRATE)
     */
    val UUID_CHAR_HEARTRATE: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val UUID_CONTROL_HEARTRATE: UUID = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")

    /**
     * Used for setting time (UUID_SERVICE_MILI)
     */
    val UUID_CHAR_DATA_TIME: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")

    /**
     * Used for getting events recorded by band (UUID_SERVICE_MILI)
     */
    val UUID_CHAR_DEVICEEVENT: UUID = UUID.fromString("00000010-0000-3512-2118-0009af100700")

}
