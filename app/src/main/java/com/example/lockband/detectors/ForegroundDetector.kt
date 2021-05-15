package com.example.lockband.detectors

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.lockband.utils.DEFAULT_TIMEOUT
import com.example.lockband.utils.hasUsageStatsPermission
import timber.log.Timber


class ForegroundDetector {

    fun getForegroundApp(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null
        Timber.d("Scan started")
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
            Timber.d("${event.packageName} in scan")
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundApp = event.packageName
            }
        }
        Timber.d("$foregroundApp on detector return")
        return foregroundApp
    }
}