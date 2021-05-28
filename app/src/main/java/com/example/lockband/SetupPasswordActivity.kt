package com.example.lockband

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.databinding.ActivitySetupPasswordBinding
import com.example.lockband.utils.PASS_FILE
import com.example.lockband.utils.getMiBandAddress
import com.example.lockband.utils.hashPassword
import com.example.lockband.utils.writeEncryptedFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_setup_password.*

/**
 * Set up activity in which user configures password for unlocking apps
 */
@AndroidEntryPoint
class SetupPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivitySetupPasswordBinding>(
            this,
            R.layout.activity_setup_password
        )

        setup_password_button.setOnClickListener {

            error_new_password.visibility = View.GONE

            if (new_password.text.toString() == repeated_new_password.text.toString()) {
                writeEncryptedFile(
                    applicationContext,
                    PASS_FILE,
                    hashPassword(new_password.text.toString())
                )
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
                error_new_password.visibility = View.VISIBLE
            }
        }
    }
}