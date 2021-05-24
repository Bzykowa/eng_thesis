package com.example.lockband.utils

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


/**
 * Extracts info from an encrypted file
 *
 * @param applicationContext Context of app
 * @param name Name of the file
 * @return Content of the file in String
 */
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

/**
 * Writes to an encrypted file
 *
 * @param applicationContext Context of the app
 * @param name Name of the file
 * @param data Information to write
 * @return Content of the file in String
 */
fun writeEncryptedFile(applicationContext: Context, name: String, data: String) {

    val encryptedFile = referenceEncryptedFile(applicationContext, name)

    encryptedFile.openFileOutput().apply {
        write(data.toByteArray(StandardCharsets.UTF_8))
        flush()
        close()
    }
}

/**
 * Access encrypted file
 *
 * @param applicationContext Context of the app
 * @param name Name of the file
 * @return EncryptedFile object
 */
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

/**
 * Hashes password for storing in the file using SHA-512
 *
 * @param pass Password to hash
 * @return Hashed Password
 */
fun hashPassword(pass: String): String {
    val message: ByteArray = pass.toByteArray(StandardCharsets.UTF_8)
    val md = MessageDigest.getInstance("SHA-512")
    return String(md.digest(message), StandardCharsets.UTF_8)
}

/**
 * Encrypts number from MiBand to authenticate connection using AES/ECB/No Padding
 *
 * @param number ByteArray received from MiBand
 * @param secretKey Key for encrypting number
 * @return Encrypted ByteArray to send back to MiBand
 */
fun encryptAES(number: ByteArray, secretKey: ByteArray): ByteArray {
    val ecipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
    val newKey = SecretKeySpec(secretKey, "AES")
    ecipher.init(Cipher.ENCRYPT_MODE, newKey)
    return ecipher.doFinal(number)
}