package com.example.lockband.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lockband.data.Actions
import com.example.lockband.utils.ServiceState
import com.example.lockband.utils.getServiceState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, LockingService::class.java).also {
                it.action = Actions.START.name
                    Log.d(null,"Starting the locking service from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
            }
        }
    }
}