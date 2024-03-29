package com.example.lockband.utils

import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class Conversions {

    companion object {

        const val TZ_FLAG_NONE = 0
        const val TZ_FLAG_INCLUDE_DST_IN_TZ = 1

        /**
         * Converts a timestamp to the byte sequence to be sent to the current time characteristic
         *
         * @param timestamp
         * @return ByteArray to send to MiBand
         */
        fun calendarToRawBytes(timestamp: Calendar): ByteArray {
            // MiBand2:
            // year,year,month,dayofmonth,hour,minute,second,dayofweek,0,0,tz
            val year = fromUint16(timestamp.get(Calendar.YEAR))
            return byteArrayOf(
                year[0],
                year[1],
                fromUint8(timestamp.get(Calendar.MONTH) + 1),
                fromUint8(timestamp.get(Calendar.DATE)),
                fromUint8(timestamp.get(Calendar.HOUR_OF_DAY)),
                fromUint8(timestamp.get(Calendar.MINUTE)),
                fromUint8(timestamp.get(Calendar.SECOND)),
                dayOfWeekToRawBytes(timestamp),
                0
            )
        }

        /**
         * Similar to calendarToRawBytes, but only up to (and including) the MINUTES.
         *
         * @param timestamp
         * @return ByteArray to send to MiBand
         */
        fun shortCalendarToRawBytes(timestamp: Calendar): ByteArray {
            // MiBand2:
            // year,year,month,dayofmonth,hour,minute
            val year = fromUint16(timestamp.get(Calendar.YEAR))
            return byteArrayOf(
                year[0],
                year[1],
                fromUint8(timestamp.get(Calendar.MONTH) + 1),
                fromUint8(timestamp.get(Calendar.DATE)),
                fromUint8(timestamp.get(Calendar.HOUR_OF_DAY)),
                fromUint8(timestamp.get(Calendar.MINUTE))
            )
        }

        /**
         * Uses the standard algorithm to convert bytes received from the MiBand to a Calendar object
         *
         * @param value
         * @return GregorianCalendar of time received from MiBand
         */
        fun rawBytesToCalendar(value: ByteArray): GregorianCalendar {
            if (value.size >= 7) {
                val year = toUint16(value[0], value[1])
                val timestamp = GregorianCalendar(
                    year,
                    (value[2].toInt() and 0xff) - 1,
                    value[3].toInt() and 0xff,
                    value[4].toInt() and 0xff,
                    value[5].toInt() and 0xff,
                    value[6].toInt() and 0xff
                )
                if (value.size > 7) {
                    val timeZone: TimeZone = TimeZone.getDefault()
                    timeZone.rawOffset = value[7] * 15 * 60 * 1000
                    timestamp.timeZone = timeZone
                }
                return timestamp
            }
            return createCalendar()
        }

        fun toUint16(vararg bytes: Byte): Int {
            return bytes[0].toInt() and 0xff or (bytes[1].toInt() and 0xff shl 8)
        }

        fun fromUint16(value: Int): ByteArray {
            return byteArrayOf(
                (value and 0xff).toByte(),
                (value shr 8 and 0xff).toByte()
            )
        }

        fun fromUint8(value: Int): Byte {
            return (value and 0xff).toByte()
        }

        /**
         * Creates a calendar object representing the current date and time.
         */
        fun createCalendar(): GregorianCalendar {
            return GregorianCalendar()
        }

        fun join(start: ByteArray?, end: ByteArray?): ByteArray? {
            if (start == null || start.isEmpty()) {
                return end
            }
            if (end == null || end.isEmpty()) {
                return start
            }

            return start + end
        }

        /**
         * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.time_zone.xml
         *
         * @param timeZone
         * @return sint8 value from -48..+56
         */
        fun mapTimeZone(timeZone: TimeZone, timezoneFlags: Int): Byte {
            var offsetMillis: Int = timeZone.rawOffset
            if (timezoneFlags == TZ_FLAG_INCLUDE_DST_IN_TZ) {
                offsetMillis += timeZone.dstSavings
            }
            val utcOffsetInHours = offsetMillis / (1000 * 60 * 60)
            return (utcOffsetInHours * 4).toByte()
        }

        private fun dayOfWeekToRawBytes(cal: Calendar): Byte {
            return when (val calValue: Int = cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> 7
                else -> (calValue - 1).toByte()
            }
        }

    }
}