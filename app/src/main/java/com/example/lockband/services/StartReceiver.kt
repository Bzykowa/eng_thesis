package com.example.lockband.services

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.data.MiBandServiceActions
import com.example.lockband.utils.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * BroadcastReceiver responsible for restarting Services after device reboot
 */
@AndroidEntryPoint
class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (getLockingServiceState(context) == LockingServiceState.STARTED) {
                Intent(context, LockingService::class.java).also {
                    it.action = LockingServiceActions.START.name
                    Timber.d("Starting the locking service from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
            }
            else {
                Intent(context, MiBandService::class.java).also {
                    it.action = MiBandServiceActions.START.name
                    it.putExtra(
                        "device", BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
                            getMiBandAddress(context)
                        )
                    )
                    Timber.d("Starting the Mi Band communication service from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
            }
        }
    }
}