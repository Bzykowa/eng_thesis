package com.example.lockband.detectors

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.example.lockband.utils.DEFAULT_TIMEOUT
import com.example.lockband.utils.hasUsageStatsPermission


class ForegroundDetector {
    fun getForegroundApp(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null
        Log.d(null,"Scan started")
        var foregroundApp: String? = null
        val time = System.currentTimeMillis()
        val usageEvents =
            (context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager).queryEvents(
                time - DEFAULT_TIMEOUT,
                time
            )
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            Log.d(null,event.packageName+" in scan")
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundApp = event.packageName
            }
        }
        Log.d(null, "$foregroundApp on detector return")
        return foregroundApp
    }
}