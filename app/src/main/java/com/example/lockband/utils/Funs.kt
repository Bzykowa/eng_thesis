package com.example.lockband.utils

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest


fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        "android:get_usage_stats",
        Process.myUid(), context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun readEncryptedFile(applicationContext: Context, name: String): String {

    val encryptedFile = referenceEncryptedFile(applicationContext, name)

    val inputStream = encryptedFile.openFileInput()
    val byteArrayOutputStream = ByteArrayOutputStream()
    var nextByte: Int = inputStream.read()
    while (nextByte != -1) {
        byteArrayOutputStream.write(nextByte)
        nextByte = inputStream.read()
    }

    return String(byteArrayOutputStream.toByteArray(),StandardCharsets.UTF_8)
}

fun writeEncryptedFile(applicationContext: Context, name: String, data: String) {

    val encryptedFile = referenceEncryptedFile(applicationContext, name)

    encryptedFile.openFileOutput().apply {
        write(data.toByteArray(StandardCharsets.UTF_8))
        flush()
        close()
    }
}

fun referenceEncryptedFile(applicationContext: Context, name: String) : EncryptedFile {
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

fun hashPassword(pass : String) : String {
    val message: ByteArray = pass.toByteArray(StandardCharsets.UTF_8)
    val md = MessageDigest.getInstance("SHA-512")
    return String(md.digest(message), StandardCharsets.UTF_8)
}
