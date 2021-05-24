package com.example.lockband.utils

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Translate Calendar date to ByteArray for MiBand
 *
 * @param calendar Calendar to parse
 * @param precision Precision of time units
 * @return ByteArray recognisable by MiBand
 */
fun getTimeBytes(calendar: Calendar, precision: TimeUnit): ByteArray? {
    val bytes: ByteArray = when {
        precision === TimeUnit.MINUTES -> {
            Conversions.shortCalendarToRawBytes(calendar)
        }
        precision === TimeUnit.SECONDS -> {
            Conversions.calendarToRawBytes(calendar)
        }
        else -> {
            throw IllegalArgumentException("Unsupported precision, only MINUTES and SECONDS are supported till now")
        }
    }
    val tail = byteArrayOf(
        0,
        Conversions.mapTimeZone(
            calendar.timeZone,
            Conversions.TZ_FLAG_INCLUDE_DST_IN_TZ
        )
    )

    return Conversions.join(bytes, tail)
}