package com.example.lockband.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.lockband.detectors.ForegroundDetector
import com.example.lockband.utils.DEFAULT_TIMEOUT
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Class connecting scanning foreground apps on another thread with managing listeners on them
 */
class AppMonitor {
    private var timeout: Long = DEFAULT_TIMEOUT
    private var service: ScheduledExecutorService? = null
    private var runnable: Runnable? = null
    private var unregisteredPackageListener: Listener? = null
    private var listeners: MutableMap<String, Listener> = HashMap()
    private var detector: ForegroundDetector = ForegroundDetector()
    private var handler: Handler = Handler(Looper.getMainLooper())

    /**
     * Interface allowing to create listener on specific app
     */
    interface Listener {
        fun onForeground(process: String?)
    }

    /**
     * Setter for period of scanning
     *
     * @param timeout Period after which scanning stops
     */
    fun timeout(timeout: Long): AppMonitor {
        this.timeout = timeout
        return this
    }

    /**
     * Puts listener for an app to a Mutable Map of Listeners
     *
     * @param packageName Package name of a blocked app
     * @param listener Listener containing method to call when app is onForeground
     */
    fun `when`(packageName: String, listener: Listener) {
        listeners[packageName] = listener
    }

    /**
     * Sets up listener for allowed apps
     *
     * @param listener Function to call when allowed app is onForeground
     */
    fun whenOther(listener: Listener?) {
        unregisteredPackageListener = listener
    }

    /**
     * Starts a Runnable which scans apps in foreground and notifies listeners
     *
     * @param context Context needed to create Runnable
     */
    fun start(context: Context) {
        runnable = createRunnable(context.applicationContext)
        service = ScheduledThreadPoolExecutor(1)
        service!!.schedule(runnable!!, timeout, TimeUnit.MILLISECONDS)
    }

    /**
     * Stops scanning immediately
     */
    fun stop() {
        if (service != null) {
            service!!.shutdownNow()
            service = null
        }
        runnable = null
    }

    /**
     * Creates Runnable responsible for scanning foreground apps and notifying listeners
     *
     * @param context Context needed for scanning apps
     * @return Runnable responsible for scanning and calling listeners
     */
    private fun createRunnable(context: Context): Runnable {
        return Runnable {
            getForegroundAppAndNotify(context)
            service!!.schedule(createRunnable(context), timeout, TimeUnit.MILLISECONDS)
        }
    }

    /**
     * Gets latest foreground app and notifies its listener
     *
     * @param context Context needed for scanning apps
     */
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
    }

    /**
     * Executes listener of specific app
     *
     * @param listener Listener containing method to run
     * @param packageName Package name of an app to run their listener
     */
    private fun callListener(listener: Listener?, packageName: String?) {
        handler.post(Runnable { listener!!.onForeground(packageName) })
    }

    /**
     * Gets name of latest app onForeground
     *
     * @param context Context needed for scanning apps
     * @return Package name of app onForeground
     */
    private fun getForegroundApp(context: Context?): String? {
        return detector.getForegroundApp(context!!)
    }
}