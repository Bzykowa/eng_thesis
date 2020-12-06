package com.example.lockband

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.data.DataGatheringServiceActions
import com.example.lockband.databinding.ActivityUnlockBinding
import com.example.lockband.services.LockingService
import com.example.lockband.services.DataGatheringService
import com.example.lockband.utils.PASS_FILE
import com.example.lockband.utils.getMiBandAddress
import com.example.lockband.utils.hashPassword
import com.example.lockband.utils.readEncryptedFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_unlock.*

@AndroidEntryPoint
class UnlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityUnlockBinding>(this, R.layout.activity_unlock)

        Intent(this, DataGatheringService::class.java).also {
            it.action = DataGatheringServiceActions.START.name
            it.putExtra(
                "device", BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
                    getMiBandAddress(this)
                )
            )
            startForegroundService(it)
        }

        unlock_button.setOnClickListener {

            errorView.visibility = View.GONE
            val hashedPass = hashPassword(passwordTextView.text.toString())
            val storedPass = retrieveStoredPassword()

            if (hashedPass == storedPass) {
                Toast.makeText(this, "Device unlocked", Toast.LENGTH_SHORT).show()

                Intent(this, LockingService::class.java).also {
                    it.action = LockingServiceActions.STOP.name
                    startForegroundService(it)
                }

                if (getMiBandAddress(this) == "err") {
                    Intent(this, PairingActivity::class.java).also {
                        startActivity(it)
                    }
                } else {
                    Intent(this, MainActivity::class.java).also {
                        startActivity(it)
                    }
                }
            } else {
                Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show()
                errorView.visibility = View.VISIBLE
            }
        }
    }

    private fun retrieveStoredPassword(): String {
        return readEncryptedFile(applicationContext, PASS_FILE)
    }
}