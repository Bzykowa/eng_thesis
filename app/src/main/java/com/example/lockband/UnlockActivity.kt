package com.example.lockband

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.transition.Visibility
import com.example.lockband.data.Actions
import com.example.lockband.databinding.ActivityMainBinding
import com.example.lockband.databinding.ActivityUnlockBinding
import com.example.lockband.services.LockingService
import com.example.lockband.utils.ServiceState
import com.example.lockband.utils.getServiceState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityUnlockBinding>(this, R.layout.activity_unlock)

        val unlockButton: ImageButton = findViewById(R.id.unlockButton)
        val passTextView: EditText = findViewById(R.id.passwordTextView)
        val error : TextView = findViewById(R.id.errorView)

        unlockButton.setOnClickListener {

            error.visibility = View.GONE
            val hashedPass = hashPassword(passTextView.text.toString())
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
                error.visibility = View.VISIBLE
            }
        }
    }

    private fun hashPassword(pass : String) : String {
        TODO("hash password")
    }

    private fun retrieveStoredPassword() : String {
        TODO("read password from somewhere")
    }
}