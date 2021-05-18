package com.example.lockband.miband3.listeners

/**
 * Action callback
 */
interface ActionCallback {

    /**
     * Called on successful completion

     * @param data Fetched data
     */
    fun onSuccess(data: Any)

    /**
     * Called on fail

     * @param errorCode Error code
     * *
     * @param msg       Error message
     */
    fun onFail(errorCode: Int, msg: String)
}
