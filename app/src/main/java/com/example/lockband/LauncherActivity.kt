package com.example.lockband

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.databinding.ActivityLauncherBinding
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

        val passFile = File(applicationContext.dataDir, PASS_FILE)
        if (!passFile.exists()) {
            Intent(this, SetupPasswordActivity::class.java).also {
                startActivity(it)
            }
        } else if (getMiBandAddress(this) == "err"){
            Intent(this, PairingActivity::class.java).also {
                startActivity(it)
            }

        } else if (getServiceState(this) == ServiceState.STOPPED) {
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
