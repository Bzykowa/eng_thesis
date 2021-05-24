package com.example.lockband.miband3.listeners

/**
 * Listener for heart rate notifications
 */
interface HeartRateNotifyListener {

    /**
     * Called when new hear rate data received
     *
     * @param heartRate Heart rate
     */
    fun onNotify(heartRate: Int)
}
