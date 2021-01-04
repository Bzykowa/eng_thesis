package com.example.lockband.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Process
import timber.log.Timber
import java.lang.Thread.sleep


//check for usage stats permission (needed to monitor apps)
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        "android:get_usage_stats",
        Process.myUid(), context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

//function used for referencing SharedPrefs
fun getPreferences(context: Context, name: String): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}


//Add delay between BLE operations while not in coroutine
fun pauseBetweenOperations() {
    try {
        sleep(OP_TIMEOUT)
    } catch (e: InterruptedException) {
        Timber.e(e)
    }
}