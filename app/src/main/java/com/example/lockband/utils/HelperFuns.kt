package com.example.lockband.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Process
import timber.log.Timber
import java.lang.Thread.sleep


/**
 * Check for usage stats permission (needed to monitor apps)
 *
 * @param context Context needed for checking if permission is granted
 * @return True if granted, False otherwise
 */
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        "android:get_usage_stats",
        Process.myUid(), context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

/**
 * Function used for referencing SharedPreferences
 *
 * @param context Context needed to access SharedPreferences
 * @param name Name of SharedPreferences to reference
 * @return SharedPreferences of specified name
 */
fun getPreferences(context: Context, name: String): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}


/**
 * Add delay between BLE operations while not in coroutine
 */
fun pauseBetweenOperations(time: Long = OP_TIMEOUT) {
    try {
        sleep(time)
    } catch (e: InterruptedException) {
        Timber.e(e)
    }
}
