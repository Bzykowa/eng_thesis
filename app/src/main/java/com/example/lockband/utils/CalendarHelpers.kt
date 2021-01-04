package com.example.lockband.utils

import java.util.*
import java.util.concurrent.TimeUnit

fun getTimeBytes(calendar: Calendar, precision: TimeUnit): ByteArray? {
    val bytes: ByteArray = when {
        precision === TimeUnit.MINUTES -> {
            CalendarConversions.shortCalendarToRawBytes(calendar)
        }
        precision === TimeUnit.SECONDS -> {
            CalendarConversions.calendarToRawBytes(calendar)
        }
        else -> {
            throw IllegalArgumentException("Unsupported precision, only MINUTES and SECONDS are supported till now")
        }
    }
    val tail = byteArrayOf(
        0,
        CalendarConversions.mapTimeZone(
            calendar.timeZone,
            CalendarConversions.TZ_FLAG_INCLUDE_DST_IN_TZ
        )
    )
    // 0 = adjust reason bitflags? or DST offset?? , timezone
    //        byte[] tail = new byte[] { 0x2 }; // reason
    return CalendarConversions.join(bytes, tail)
}