package com.example.lockband.miband3.listeners

/**
 * Listener for data notifications

 * @author Dmytro Khmelenko
 */
interface NotifyListener {

    /**
     * Called when new data arrived

     * @param data Binary data
     */
    fun onNotify(data: ByteArray)
}
