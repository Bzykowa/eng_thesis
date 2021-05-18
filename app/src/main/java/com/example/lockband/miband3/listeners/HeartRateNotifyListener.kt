package com.example.lockband.miband3.listeners

/**
 * Listener for hear rate notifications
 */
interface HeartRateNotifyListener {

    /**
     * Called when new hear rate data received
     *
     * @param heartRate Hear rate
     */
    fun onNotify(heartRate: Int)
}
