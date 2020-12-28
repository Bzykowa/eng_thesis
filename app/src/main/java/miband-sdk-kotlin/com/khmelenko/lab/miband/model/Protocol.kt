package com.khmelenko.lab.miband.model

import com.example.lockband.utils.KEY
import com.example.lockband.utils.parseHexBinary

/**
 * Defines values for accessing data and controlling band
 *
 * @author Dmytro Khmelenko
 */
object Protocol {

    val NOTIFY_ENABLE = byteArrayOf(1, 0)
    val NOTIFY_DISABLE = byteArrayOf(0, 0)
    val SEND_KEY = parseHexBinary("0108$KEY")!!     //byteArrayOf(1,8,1,23,45,67,89,1,22,23,34,45,56,78,89,90,2)
    val REQ_RAND_NUMBER = parseHexBinary("0208")!!
    val SEND_ENC_NUMBER = parseHexBinary("0308")!!
    val VIBRATION_WITH_LED = byteArrayOf(1)
    val VIBRATION_10_TIMES_WITH_LED = byteArrayOf(2)
    val VIBRATION_WITHOUT_LED = byteArrayOf(4)
    val STOP_VIBRATION = byteArrayOf(0)
    val ENABLE_REALTIME_STEPS_NOTIFY = byteArrayOf(3, 1)
    val DISABLE_REALTIME_STEPS_NOTIFY = byteArrayOf(3, 0)
    val ENABLE_SENSOR_DATA_NOTIFY = parseHexBinary("010319")!!
    val DISABLE_SENSOR_DATA_NOTIFY = byteArrayOf(18, 0)
    val SET_COLOR_RED = byteArrayOf(14, 6, 1, 2, 1)
    val SET_COLOR_BLUE = byteArrayOf(14, 0, 6, 6, 1)
    val SET_COLOR_ORANGE = byteArrayOf(14, 6, 2, 0, 1)
    val SET_COLOR_GREEN = byteArrayOf(14, 4, 5, 0, 1)
    val START_HEART_RATE_SCAN = byteArrayOf(21, 2, 1)
    val DISABLE_HEART_RATE_SCAN = byteArrayOf(21, 2, 0)

    val REBOOT = byteArrayOf(12)
    val REMOTE_DISCONNECT = byteArrayOf(1)
    val FACTORY_RESET = byteArrayOf(9)
    val SELF_TEST = byteArrayOf(2)
}
