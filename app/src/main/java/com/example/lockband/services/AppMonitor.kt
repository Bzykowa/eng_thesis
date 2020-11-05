package com.example.lockband.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.lockband.detectors.ForegroundDetector
import com.example.lockband.utils.DEFAULT_TIMEOUT
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class AppMonitor {
    private var timeout: Long = DEFAULT_TIMEOUT
    private var service: ScheduledExecutorService? = null
    private var runnable: Runnable? = null
    private var unregisteredPackageListener: Listener? = null
    private var anyPackageListener: Listener? = null
    private var listeners: MutableMap<String, Listener> = HashMap()
    private var detector: ForegroundDetector = ForegroundDetector()
    private var handler: Handler = Handler(Looper.getMainLooper())

    interface Listener {
        fun onForeground(process: String?)
    }

    fun timeout(timeout: Long): AppMonitor {
        this.timeout = timeout
        return this
    }

    fun `when`(packageName: String, listener: Listener) {
        listeners[packageName] = listener
    }

    fun whenOther(listener: Listener?) {
        unregisteredPackageListener = listener
    }

    fun whenAny(listener: Listener?) {
        anyPackageListener = listener
    }

    fun start(context: Context) {
        runnable = createRunnable(context.applicationContext)
        service = ScheduledThreadPoolExecutor(1)
        service!!.schedule(runnable!!, timeout, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        if (service != null) {
            service!!.shutdownNow()
            service = null
        }
        runnable = null
    }

    private fun createRunnable(context: Context): Runnable {
        return Runnable {
            getForegroundAppAndNotify(context)
            service!!.schedule(createRunnable(context), timeout, TimeUnit.MILLISECONDS)
        }
    }

    private fun getForegroundAppAndNotify(context: Context) {
        val foregroundApp = getForegroundApp(context)
        var foundRegisteredPackageListener = false
        if (foregroundApp != null) {
            for (packageName in listeners.keys) {
                if (packageName.equals(foregroundApp, ignoreCase = true)) {
                    foundRegisteredPackageListener = true
                    callListener(listeners[foregroundApp], foregroundApp)
                }
            }
            if (!foundRegisteredPackageListener && unregisteredPackageListener != null) {
                callListener(unregisteredPackageListener, foregroundApp)
            }
        }
        if (anyPackageListener != null) {
            callListener(anyPackageListener, foregroundApp)
        }
    }

    fun callListener(listener: Listener?, packageName: String?) {
        handler.post(Runnable { listener!!.onForeground(packageName) })
    }

    fun getForegroundApp(context: Context?): String? {
        return detector.getForegroundApp(context!!)
    }
}