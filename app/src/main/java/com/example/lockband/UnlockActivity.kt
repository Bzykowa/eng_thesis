package com.example.lockband

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.databinding.ActivityUnlockBinding
import com.example.lockband.services.LockingService
import com.example.lockband.utils.PASS_FILE
import com.example.lockband.utils.getMiBandAddress
import com.example.lockband.utils.hashPassword
import com.example.lockband.utils.readEncryptedFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_unlock.*

/**
 * Activity which appears when user tries to open locked app. It prompts user to enter password to unlock access.
 */
@AndroidEntryPoint
class UnlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityUnlockBinding>(this, R.layout.activity_unlock)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)


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

                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

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

    override fun onBackPressed() {
        return
    }

    private fun retrieveStoredPassword(): String {
        return readEncryptedFile(applicationContext, PASS_FILE)
    }
}