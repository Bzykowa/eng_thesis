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
import com.example.lockband.R
import com.example.lockband.UnlockActivity
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.data.MiBandServiceActions
import com.example.lockband.data.room.AppStateRepository
import com.example.lockband.utils.DEFAULT_TIMEOUT
import com.example.lockband.utils.ServiceState
import com.example.lockband.utils.setServiceState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LockingService : Service() {

    @Inject
    lateinit var appStateRepository: AppStateRepository

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private val appMonitor = AppMonitor()


    override fun onBind(intent: Intent?): IBinder? {
        //not binding to anything
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                LockingServiceActions.START.name -> {
                    Intent(this, MiBandService::class.java).also {
                        it.action = MiBandServiceActions.STOP.name
                        startForegroundService(it)
                    }
                    startService()
                }
                LockingServiceActions.STOP.name -> stopService()
                else -> Timber.e("This should never happen. No action in the received intent")
            }
        } else {
            Timber.d(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("The locking service has been created")
        val notification = createNotification()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("The locking service has been destroyed")
        Toast.makeText(this, "LockingService destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, LockingService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

    private fun startService() {
        if (isServiceStarted) return
        Timber.d("Starting the foreground service task")
        Toast.makeText(this, "Lockdown started", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // lock to avoid being affected by Doze Mode
        wakeLock =
            (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockingService::lock").apply {
                    acquire()
                }
            }

        buildAppMonitor()
        GlobalScope.launch(Dispatchers.Default) {
            while (isServiceStarted) {
                launch(Dispatchers.Default) {
                    appMonitor.start(this@LockingService)
                }
                delay(DEFAULT_TIMEOUT)
            }
        }


    }

    private fun stopService() {
        Timber.d("Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            appMonitor.stop()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Timber.d("Service stopped without being started: ${e.message}")
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
            "Locking Service notifications channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "Locking Service channel"
            it.enableLights(true)
            it.lightColor = Color.RED
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            it
        }
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent =
            Intent(this, UnlockActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder = Notification.Builder(
            this,
            notificationChannelId
        )

        return builder
            .setContentTitle("Lockdown")
            .setContentText("Some applications have been blocked.")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.outline_app_blocking_black_18dp)
            .setTicker("Ur acting kinda sus")
            .build()
    }

    private fun buildAppMonitor(): AppMonitor {

        GlobalScope.launch(Dispatchers.IO) {
            val lockedApps = appStateRepository.getLockedApps()

            lockedApps.forEach { app ->
                Timber.d("%s in locked apps", app)
                appMonitor.`when`(app, object : AppMonitor.Listener {
                    override fun onForeground(process: String?) {
                        Timber.d("%s on FG", app)
                        Intent(this@LockingService, UnlockActivity::class.java).also {
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(it)
                        }
                    }
                })
            }
        }

        appMonitor.apply {
            whenOther(object : AppMonitor.Listener {
                override fun onForeground(process: String?) {
                    Timber.d("allowed app")
                }

            })
            timeout(DEFAULT_TIMEOUT)
        }

        return appMonitor
    }

}