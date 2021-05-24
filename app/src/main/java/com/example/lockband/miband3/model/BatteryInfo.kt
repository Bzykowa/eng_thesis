package com.example.lockband.miband3.model

import com.example.lockband.utils.Conversions
import java.text.SimpleDateFormat
import java.util.*

/**
 * Battery info model
 */
class BatteryInfo constructor(
    val level: Int,
    val cycles: Int,
    val status: Status?,
    val lastChargedDate: Calendar = Calendar.getInstance()
) {

    enum class Status {
        NORMAL, CHARGING, UNKNOWN;

        companion object {

            fun fromByte(b: Byte): Status {
                return when (b.toInt()) {
                    0 -> NORMAL
                    1 -> CHARGING
                    else -> UNKNOWN
                }
            }
        }
    }

    override fun toString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.getDefault())
        val formattedDate = formatter.format(lastChargedDate.time)
        return "cycles:" + cycles +
                ",level:" + level +
                ",status:" + status +
                ",last:" + formattedDate
    }

    companion object {

        /**
         * Creates an instance of the battery info from byte data

         * @param data Byte data
         * *
         * @return Battery info
         */
        fun fromByteData(data: ByteArray): BatteryInfo {
            val level = if (data.size >= 2) data[1].toInt() else 50
            val status = if (data.size >= 3) Status.fromByte(data[2]) else Status.UNKNOWN
            val cycles =
                if (data.size >= 10) 0xffff and (0xff and data[7].toInt() or (0xff and data[8].toInt() shl 8)) else -1

            val lastChargeDay = if (data.size >= 18) Conversions.rawBytesToCalendar(
                byteArrayOf(
                    data[10],
                    data[11],
                    data[12],
                    data[13],
                    data[14],
                    data[15],
                    data[16],
                    data[17]
                )
            ) else GregorianCalendar.getInstance()

            return BatteryInfo(level, cycles, status, lastChargeDay)
        }
    }

}
