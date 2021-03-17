package com.example.lockband.miband3.model

import com.example.lockband.utils.KEY
import com.example.lockband.utils.parseHexBinary

/**
 * Defines values for accessing data and controlling band
 *
 * @author Dmytro Khmelenko
 */
object Protocol {

    //Notification setup
    val NOTIFY_ENABLE = byteArrayOf(1, 0)
    val NOTIFY_DISABLE = byteArrayOf(0, 0)

    //Authentication protocol
    val SEND_KEY = byteArrayOf(1, 8) + KEY
    val REQ_RAND_NUMBER = byteArrayOf(2, 8)
    val SEND_ENC_NUMBER = byteArrayOf(3, 8)

    //Vibration protocol
    val VIBRATION_WITH_LED = byteArrayOf(1)
    val VIBRATION_10_TIMES_WITH_LED = byteArrayOf(2)
    val VIBRATION_WITHOUT_LED = byteArrayOf(4)
    val STOP_VIBRATION = byteArrayOf(0)

    //Heart Rate Protocol
    val PING_HR_MONITOR = parseHexBinary("16")!!
    val ENABLE_HR_SCAN_AUTO: ByteArray = byteArrayOf(21, 1, 1)
    val DISABLE_HEART_RATE_SCAN_AUTO = byteArrayOf(21, 1, 0)
    val START_HR_SCAN_MANUAL: ByteArray = byteArrayOf(21, 2, 1)
    val SET_HR_SLEEP_SUPPORT = byteArrayOf(21, 0, 1)
    val SET_HR_MEASURE_INTERVAL = byteArrayOf(20, 1)

    //Steps protocol (fix)
    val ENABLE_REALTIME_STEPS_NOTIFY = byteArrayOf(3, 1)
    val DISABLE_REALTIME_STEPS_NOTIFY = byteArrayOf(3, 0)

    //Sensor protocol (fix or del)
    val ENABLE_SENSOR_DATA_NOTIFY = parseHexBinary("010319")!!
    val DISABLE_SENSOR_DATA_NOTIFY = byteArrayOf(18, 0)

    //Alert LED colors protocol
    val SET_COLOR_RED = byteArrayOf(14, 6, 1, 2, 1)
    val SET_COLOR_BLUE = byteArrayOf(14, 0, 6, 6, 1)
    val SET_COLOR_ORANGE = byteArrayOf(14, 6, 2, 0, 1)
    val SET_COLOR_GREEN = byteArrayOf(14, 4, 5, 0, 1)

    //Band settings
    const val ENDPOINT_DISPLAY: Byte = 6
    val SET_ENGLISH_LANGUAGE: ByteArray = parseHexBinary("061700656e5f5553")!!
    val DATEFORMAT_DATE_TIME = byteArrayOf(ENDPOINT_DISPLAY, 0x0a, 0, 3)
    val DATEFORMAT_TIME = byteArrayOf(ENDPOINT_DISPLAY, 0x0a, 0, 0)
    val DATEFORMAT_TIME_24_HOURS = byteArrayOf(ENDPOINT_DISPLAY, 2, 0, 1)
    val COMMAND_DISTANCE_UNIT_METRIC = byteArrayOf(ENDPOINT_DISPLAY, 3, 0, 0)
    const val COMMAND_SET_USERINFO : Byte = 0x4f
    val WEAR_LOCATION_LEFT_WRIST = byteArrayOf( 0x20, 0x00, 0x00, 0x02)


    val REBOOT = byteArrayOf(12)
    val REMOTE_DISCONNECT = byteArrayOf(1)
    val FACTORY_RESET = byteArrayOf(9)
    val SELF_TEST = byteArrayOf(2)
}
