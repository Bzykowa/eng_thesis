package com.example.lockband.detectors

import android.app.Service
import android.app.usage.UsageEvents

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.lockband.utils.hasUsageStatsPermission


class ForegroundDetector {
    fun getForegroundApp(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null
        var foregroundApp: String? = null
        val time = System.currentTimeMillis()
        val usageEvents =
            (context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager).queryEvents(
                time - 1000 * 3600,
                time
            )
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundApp = event.packageName
            }
        }
        return foregroundApp
    }
}