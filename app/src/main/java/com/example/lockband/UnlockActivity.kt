package com.example.lockband

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.data.Actions
import com.example.lockband.databinding.ActivityUnlockBinding
import com.example.lockband.services.LockingService
import com.example.lockband.utils.PASS_FILE
import com.example.lockband.utils.hashPassword
import com.example.lockband.utils.readEncryptedFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_unlock.*

@AndroidEntryPoint
class UnlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityUnlockBinding>(this, R.layout.activity_unlock)

        unlock_button.setOnClickListener {

            errorView.visibility = View.GONE
            val hashedPass = hashPassword(passwordTextView.text.toString())
            val storedPass = retrieveStoredPassword()

            if(hashedPass == storedPass){
                Toast.makeText(this,"Device unlocked",Toast.LENGTH_SHORT).show()

                Intent(this, LockingService::class.java).also {
                    it.action = Actions.STOP.name
                    startForegroundService(it)
                }

                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                Toast.makeText(this,"Operation failed",Toast.LENGTH_SHORT).show()
                errorView.visibility = View.VISIBLE
            }
        }
    }

    private fun retrieveStoredPassword() : String {
        return readEncryptedFile(applicationContext, PASS_FILE)
    }
}