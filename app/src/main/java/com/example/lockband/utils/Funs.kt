package com.example.lockband.utils

import android.app.AppOpsManager
import android.content.Context
import android.os.Process


fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        "android:get_usage_stats",
        Process.myUid(), context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}