package com.example.lockband

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.lockband.databinding.ActivityMainBinding
import com.example.lockband.utils.LockingServiceState
import com.example.lockband.utils.getLockingServiceState
import com.example.lockband.utils.pauseBetweenOperations
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity of application which contains bottom navigation and fragment container
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        bottom_nav.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        if (getLockingServiceState(this) == LockingServiceState.STARTED){
            Intent(this, UnlockActivity::class.java).also {
                startActivity(it)
            }
        }
    }

}


