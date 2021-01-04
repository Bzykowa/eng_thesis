package com.example.lockband.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Process
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Thread.sleep
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


//check for usage stats permission (needed to monitor apps)
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        "android:get_usage_stats",
        Process.myUid(), context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

//Encrypted password file

fun readEncryptedFile(applicationContext: Context, name: String): String {

    val encryptedFile = referenceEncryptedFile(applicationContext, name)

    val inputStream = encryptedFile.openFileInput()
    val byteArrayOutputStream = ByteArrayOutputStream()
    var nextByte: Int = inputStream.read()
    while (nextByte != -1) {
        byteArrayOutputStream.write(nextByte)
        nextByte = inputStream.read()
    }

    return String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8)
}

fun writeEncryptedFile(applicationContext: Context, name: String, data: String) {

    val encryptedFile = referenceEncryptedFile(applicationContext, name)

    encryptedFile.openFileOutput().apply {
        write(data.toByteArray(StandardCharsets.UTF_8))
        flush()
        close()
    }
}

fun referenceEncryptedFile(applicationContext: Context, name: String): EncryptedFile {
    val mainKey = MasterKey.Builder(applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedFile.Builder(
        applicationContext,
        File(applicationContext.dataDir, name),
        mainKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()
}

fun hashPassword(pass: String): String {
    val message: ByteArray = pass.toByteArray(StandardCharsets.UTF_8)
    val md = MessageDigest.getInstance("SHA-512")
    return String(md.digest(message), StandardCharsets.UTF_8)
}

fun getPreferences(context: Context, name: String): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}

fun parseHexBinary(s: String): ByteArray? {
    val len = s.length

    // "111" is not a valid hex encoding.
    require(len % 2 == 0) { "hexBinary needs to be even-length: $s" }
    val out = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        val h = hexToBin(s[i])
        val l = hexToBin(s[i + 1])
        require(!(h == -1 || l == -1)) { "contains illegal character for hexBinary: $s" }
        out[i / 2] = (h * 16 + l).toByte()
        i += 2
    }
    return out
}

private fun hexToBin(ch: Char): Int {
    if (ch in '0'..'9') return ch - '0'
    if (ch in 'A'..'F') return ch - 'A' + 10
    return if (ch in 'a'..'f') ch - 'a' + 10 else -1
}

private val hexCode = "0123456789ABCDEF".toCharArray()

fun printHexBinary(data: ByteArray): String? {
    val r = StringBuilder(data.size * 2)
    for (b in data) {
        r.append(hexCode[b.toInt() shr 4 and 0xF])
        r.append(hexCode[b.toInt() and 0xF])
    }
    return r.toString()
}

fun encryptAES(number: ByteArray, secretKey: ByteArray): ByteArray {
    val ecipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
    val newKey = SecretKeySpec(secretKey, "AES")
    ecipher.init(Cipher.ENCRYPT_MODE, newKey)
    return ecipher.doFinal(number)
}

fun pauseBetweenOperations() {
    try {
        sleep(OP_TIMEOUT)
    } catch (e: InterruptedException) {
        Timber.e(e)
    }

}