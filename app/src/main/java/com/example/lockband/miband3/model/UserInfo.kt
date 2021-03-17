package com.example.lockband.miband3.model

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * User information
 */
class UserInfo constructor(
    private val uid: Int,
    private val year: Int,
    private val month: Byte,
    private val day: Byte,
    private val sex: Byte,
    private val height: Int,
    private val weight: Int
) {

    fun getBytes(): ByteArray {
        return byteArrayOf(
            Protocol.COMMAND_SET_USERINFO,
            0,
            0,
            (year and 0xff).toByte(),
            (year shr 8 and 0xff).toByte(),
            month,
            day,
            sex,
            (height and 0xff).toByte(),
            (height shr 8 and 0xff).toByte(),
            (weight * 200 and 0xff).toByte(),
            (weight * 200 shr 8 and 0xff).toByte(),
            (uid and 0xff).toByte(),
            (uid shr 8 and 0xff).toByte(),
            (uid shr 16 and 0xff).toByte(),
            (uid shr 24 and 0xff).toByte()
        )
    }

    override fun toString(): String {
        return "uid:" + uid +
                ",sex:" + sex +
                ",dateOfBirth:" + year + "/" + month + "/" + day +
                ",height:" + height +
                ",weight:" + weight
    }

}
