package com.example.lockband

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.lockband.data.DataGatheringServiceActions
import com.example.lockband.databinding.ActivityLauncherBinding
import com.example.lockband.services.MiBandService
import com.example.lockband.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLauncherBinding>(this, R.layout.activity_launcher)

        if (needsUsageStatsPermission()) {
            requestUsageStatsPermission()
        }

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    eventsAfterPermissionCheck()
                } else {
                    finish()
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                eventsAfterPermissionCheck()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            -> {
                AlertDialog.Builder(this)
                    .setTitle("Location permission")
                    .setMessage("Communication with band requires location access.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                    .create()
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }

    }

    private fun eventsAfterPermissionCheck(){
        val passFile = File(applicationContext.dataDir, PASS_FILE)
        if (!passFile.exists()) {
            Intent(this, SetupPasswordActivity::class.java).also {
                startActivity(it)
            }
        } else if (getMiBandAddress(this) == "err") {
            Intent(this, PairingActivity::class.java).also {
                startActivity(it)
            }

        } else if (getServiceState(this) == ServiceState.STOPPED) {
            Intent(this, MiBandService::class.java).also {
                it.action = DataGatheringServiceActions.START.name
                it.putExtra(
                    "device", BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
                        getMiBandAddress(this)
                    )
                )
                startForegroundService(it)
            }
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        } else {
            Intent(this, UnlockActivity::class.java).also {
                startActivity(it)
            }
        }

    }

    private fun needsUsageStatsPermission(): Boolean {
        return !hasUsageStatsPermission(this)
    }

    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}
