package com.example.lockband.services

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.widget.Toast
import com.example.lockband.data.MiBandServiceActions
import com.example.lockband.data.room.HeartRate
import com.example.lockband.data.room.HeartRateRepository
import com.example.lockband.data.room.SensorDataRepository
import com.example.lockband.data.room.StepRepository
import com.example.lockband.utils.MiBandServiceState
import com.example.lockband.utils.setMiBandBatteryInfo
import com.example.lockband.utils.setMiBandServiceState
import com.khmelenko.lab.miband.MiBand
import com.khmelenko.lab.miband.listeners.HeartRateNotifyListener
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MiBandService : Service() {

    @Inject
    lateinit var stepRepository: StepRepository

    @Inject
    lateinit var heartRateRepository: HeartRateRepository

    @Inject
    lateinit var sensorDataRepository: SensorDataRepository

    private var miBand = MiBand(this)
    private val disposables = CompositeDisposable()

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                MiBandServiceActions.PAIR.name -> handlePairing(intent)
                MiBandServiceActions.START.name -> {
                    actionConnect(intent)
                    startService()
                }
                MiBandServiceActions.STOP.name -> stopService()
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
        Timber.d("The Mi Band communication service has been created")
        val notification = createNotification()
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("The Mi Band communication service has been destroyed")
        Toast.makeText(this, "MiBandService destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, MiBandService::class.java).also {
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

    //Intent action handlers

    private fun startService() {
        if (isServiceStarted) return
        Timber.d("Starting the foreground service task")
        Toast.makeText(this, "Communication with band started", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setMiBandServiceState(this, MiBandServiceState.STARTED)

        // lock to avoid being affected by Doze Mode
        wakeLock =
            (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiBandService::lock").apply {
                    acquire()
                }
            }

        //TODO("set up observables")


        //TODO("Set up broadcast receiver and parse these intents")
        /*
        MiBandServiceActions.BATTERY.name -> handleBatteryUpdate()
                MiBandServiceActions.ALERT.name -> handleAlert()
         */


    }

    private fun stopService() {
        disposables.clear()
    }

    private fun handlePairing(intent: Intent?) {
        actionConnect(intent)

        val d = miBand.pair().subscribe(
            { Timber.d("Pairing successful") },
            { throwable -> Timber.e(throwable, "Pairing failed") })
        disposables.add(d)

        startService()
    }

    private fun handleBatteryUpdate() {
        val d = miBand.batteryInfo
            .subscribe({ batteryInfo ->
                setMiBandBatteryInfo(this, batteryInfo)
                Timber.d(batteryInfo.toString())
            },
                { throwable -> Timber.e(throwable, "getBatteryInfo fail") })
        disposables.add(d)
    }

    private fun handleAlert() {
        TODO("Not yet implemented")
    }

    //Communication action handlers

    private fun actionConnect(intent: Intent?) {
        val device = intent!!.getParcelableExtra<BluetoothDevice>("device")

        val d = miBand.connect(device!!)
            .subscribe({ result ->
                Timber.d("Connect onNext: $result")
            }, { throwable ->
                throwable.printStackTrace()
                Timber.e(throwable)
            })
        disposables.add(d)
    }

    fun actionSetHeartRateNotifyListener() {
        miBand.setHeartRateScanListener(object : HeartRateNotifyListener {
            override fun onNotify(heartRate: Int) {
                Timber.d("heart rate: $heartRate")

                val now = Calendar.getInstance()

                GlobalScope.launch(Dispatchers.IO) {
                    heartRateRepository.insertHeartRateSample(HeartRate(now,heartRate))
                }
            }
        })
    }

    private fun createNotification(): Notification {

    }
}
