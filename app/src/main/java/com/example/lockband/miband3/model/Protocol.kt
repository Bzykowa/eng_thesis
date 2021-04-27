package com.example.lockband.miband3.model

import com.example.lockband.utils.KEY
import com.example.lockband.utils.parseHexBinary

/**
 * Defines values for accessing data and controlling band
 *
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

    //Device Events
    const val FELL_ASLEEP: Byte = 0x01
    const val START_NONWEAR: Byte = 0x06

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
    private const val ENDPOINT_DISPLAY: Byte = 6
    val COMMAND_REQUEST_ALARMS = byteArrayOf(0x0d)
    val SET_ENGLISH_LANGUAGE: ByteArray = parseHexBinary("061700656e5f5553")!!
    val COMMAND_DISABLE_BAND_SCREEN_UNLOCK = byteArrayOf(ENDPOINT_DISPLAY, 0x16, 0, 0)
    val COMMAND_NIGHT_MODE_OFF = byteArrayOf(0x1a, 0x00)
    val DATEFORMAT_DATE_DD_MM_YYYY = byteArrayOf(
        ENDPOINT_DISPLAY,
        30,
        0x00,
        'd'.toByte(),
        'd'.toByte(),
        '/'.toByte(),
        'M'.toByte(),
        'M'.toByte(),
        '/'.toByte(),
        'y'.toByte(),
        'y'.toByte(),
        'y'.toByte(),
        'y'.toByte()
    )
    val DATEFORMAT_DATE_TIME = byteArrayOf(ENDPOINT_DISPLAY, 0x0a, 0, 3)
    val DATEFORMAT_TIME = byteArrayOf(ENDPOINT_DISPLAY, 0x0a, 0, 0)
    val DATEFORMAT_TIME_24_HOURS = byteArrayOf(ENDPOINT_DISPLAY, 2, 0, 1)
    val COMMAND_DISTANCE_UNIT_METRIC = byteArrayOf(ENDPOINT_DISPLAY, 3, 0, 0)
    const val COMMAND_SET_USERINFO: Byte = 0x4f
    val WEAR_LOCATION_LEFT_WRIST = byteArrayOf(0x20, 0x00, 0x00, 0x02)
    val COMMAND_SET_FITNESS_GOAL_START = byteArrayOf(0x10, 0x0, 0x0)
    val COMMAND_SET_FITNESS_GOAL_END = byteArrayOf(0, 0)
    val DISPLAY_ITEMS_DEFAULT = parseHexBinary("0a7f30000102030405060708")!!
    val COMMAND_DO_NOT_DISTURB_OFF = byteArrayOf(0x09, 0x82.toByte())
    val COMMAND_DISABLE_ROTATE_WRIST_TO_SWITCH_INFO =
        byteArrayOf(ENDPOINT_DISPLAY, 0x0d, 0x00, 0x00)
    val COMMAND_DISABLE_DISPLAY_ON_LIFT_WRIST = byteArrayOf(ENDPOINT_DISPLAY, 0x05, 0x00, 0x00)
    val COMMAND_ENABLE_DISPLAY_CALLER = byteArrayOf(ENDPOINT_DISPLAY, 0x10, 0x00, 0x00, 0x01)
    val COMMAND_DISABLE_GOAL_NOTIFICATION = byteArrayOf(ENDPOINT_DISPLAY, 0x06, 0x00, 0x00)
    val COMMAND_DISABLE_INACTIVITY_WARNINGS =
        byteArrayOf(0x08, 0x00, 0x3c, 0x00, 0x04, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00)
    val COMMAND_ENABLE_DISCONNECT_NOTIFCATION =
        byteArrayOf(ENDPOINT_DISPLAY, 0x0c, 0x00, 0x01, 0, 0, 0, 0)
    val COMMAND_ENABLE_BT_CONNECTED_ADVERTISEMENT = byteArrayOf(ENDPOINT_DISPLAY, 0x01, 0x00, 0x01)


    val REBOOT = byteArrayOf(12)
    val REMOTE_DISCONNECT = byteArrayOf(1)
    val FACTORY_RESET = byteArrayOf(9)
    val SELF_TEST = byteArrayOf(2)

    //Map of band config actions
    val actions: Map<ByteArray, String> =
        mapOf(
            SET_ENGLISH_LANGUAGE.sliceArray(0..2) to "Set language",
            COMMAND_DISABLE_BAND_SCREEN_UNLOCK.sliceArray(0..2) to "Disabled screen unlock",
            COMMAND_NIGHT_MODE_OFF to "Disabled night mode",
            DATEFORMAT_DATE_DD_MM_YYYY.sliceArray(0..2) to "Set date format",
            DATEFORMAT_DATE_TIME.sliceArray(0..2) to "Set date display",
            DATEFORMAT_TIME_24_HOURS.sliceArray(0..2) to "Set 24h time format",
            COMMAND_DISTANCE_UNIT_METRIC.sliceArray(0..2) to "Set metric units",
            DISPLAY_ITEMS_DEFAULT.sliceArray(0..0) to "Set display items",
            COMMAND_DO_NOT_DISTURB_OFF to "Disable DND",
            COMMAND_DISABLE_ROTATE_WRIST_TO_SWITCH_INFO.sliceArray(0..2) to "Disable rotate wrist to switch info",
            COMMAND_DISABLE_DISPLAY_ON_LIFT_WRIST.sliceArray(0..2) to "Disable enabling display on lifting wrist",
            COMMAND_ENABLE_DISPLAY_CALLER.sliceArray(0..2) to "Enable display caller",
            COMMAND_DISABLE_GOAL_NOTIFICATION.sliceArray(0..2) to "Disable goal notification",
            COMMAND_DISABLE_INACTIVITY_WARNINGS.sliceArray(0..0) to "Disable inactivity warnings",
            COMMAND_ENABLE_DISCONNECT_NOTIFCATION.sliceArray(0..2) to "Enable disconnect notification",
            COMMAND_ENABLE_BT_CONNECTED_ADVERTISEMENT.sliceArray(0..2) to "Enable BT connected advertisement",
            COMMAND_REQUEST_ALARMS to "Requested alarms"
        )
}
