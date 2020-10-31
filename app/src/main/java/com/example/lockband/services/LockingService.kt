package com.example.lockband.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.lockband.MainActivity
import com.example.lockband.R
import com.example.lockband.utils.Actions
import com.example.lockband.utils.ServiceState
import com.example.lockband.utils.setServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false


    override fun onBind(intent: Intent?): IBinder? {
        //not binding to anything
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> Log.e(null, "This should never happen. No action in the received intent")
            }
        } else {
            Log.d(
                null,
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(null, "The locking service has been created")
        val notification = createNotification()
        startForeground(1, notification)
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(null, "The locking service has been destroyed")
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, LockingService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        );
    }

    private fun startService() {
        if (isServiceStarted) return
        Log.d(null, "Starting the foreground service task")
        Toast.makeText(this, "Lockdown started", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // lock to avoid being affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // Check for foreground apps in a coroutine loop and end them if they should be blocked
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    TODO("Locking code here")
                }
                delay(200)
            }
        }
    }

    private fun stopService() {
        Log.d(null, "Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(null,"Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "LOCKING SERVICE CHANNEL"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId,
            "Endless Service notifications channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "Endless Service channel"
            it.enableLights(true)
            it.lightColor = Color.RED
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            it
        }
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder = Notification.Builder(
            this,
            notificationChannelId
        )

        return builder
            .setContentTitle("Lockdown")
            .setContentText("Some applications have been blocked. Enter Lockband app to unblock.")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.outline_app_blocking_black_18dp)
            .setTicker("Ur acting kinda sus")
            .build()
    }

}