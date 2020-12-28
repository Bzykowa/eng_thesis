package com.example.lockband.services

import android.app.*
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.widget.Toast
import com.example.lockband.MainActivity
import com.example.lockband.R
import com.example.lockband.data.DataGatheringServiceActions
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.data.room.entities.BandStep
import com.example.lockband.data.room.entities.HeartRate
import com.example.lockband.data.room.entities.PhoneStep
import com.example.lockband.data.room.entities.SensorData
import com.example.lockband.data.room.repos.HeartRateRepository
import com.example.lockband.data.room.repos.SensorDataRepository
import com.example.lockband.data.room.repos.StepRepository
import com.example.lockband.utils.*
import com.khmelenko.lab.miband.MiBand
import com.khmelenko.lab.miband.listeners.HeartRateNotifyListener
import com.khmelenko.lab.miband.listeners.RealtimeStepsNotifyListener
import com.khmelenko.lab.miband.model.VibrationMode
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DataGatheringService : Service(), SensorEventListener {

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

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleBatteryUpdate()
        }
    }

    private val alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleAlert()
        }
    }

    private val batteryIntentFilter = IntentFilter(DataGatheringServiceActions.BATTERY.name)
    private val alertIntentFilter = IntentFilter(DataGatheringServiceActions.ALERT.name)


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                DataGatheringServiceActions.PAIR.name -> actionConnect(intent)
                DataGatheringServiceActions.START.name -> {
                    actionConnect(intent)
                }
                DataGatheringServiceActions.STOP.name -> stopService()
                else -> Timber.e("This should never happen. No action in the received intent")
            }
        } else {
            Timber.d(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        // restarted if the system kills the service
        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
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
        val restartServiceIntent =
            Intent(applicationContext, DataGatheringService::class.java).also {
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
        isServiceStarted = true
        setMiBandServiceState(this, MiBandServiceState.STARTED)

        // lock to avoid being affected by Doze Mode
        wakeLock =
            (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiBandService::lock").apply {
                    acquire()
                }
            }

        //set up listeners for band data scans
        miBand.removePairingListener()
        actionSetRealtimeStepsNotifyListener()
        actionSetSensorDataNotifyListener()
        actionSetHeartRateNotifyListener()
        actionEnableSensorDataNotify()
        actionStartMeasuringSensorData()

        //Register BroadcastReceivers for battery update and vibrating alerts
        registerReceiver(batteryReceiver, batteryIntentFilter)
        registerReceiver(alertReceiver, alertIntentFilter)

        //Periodically scan heart rate
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                actionStartHeartRateScan()
                delay(HR_TIMEOUT)
            }
        }


    }

    private fun stopService() {
        Timber.d("Stopping the mi band foreground service")
        Toast.makeText(this, "Mi Band Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            unregisterReceiver(batteryReceiver)
            unregisterReceiver(alertReceiver)

            disposables.clear()

            sensorManager.unregisterListener(this)

            stopForeground(true)
            stopSelf()

        } catch (e: Exception) {
            Timber.d("Mi Band Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setMiBandServiceState(this, MiBandServiceState.STOPPED)
    }

    private fun handleAuthentication() {
        Timber.d("Starting to pair")

        disposables.add(
            miBand.initializePairing().delaySubscription(2, TimeUnit.SECONDS).subscribe({ result ->
                Timber.d("Sent key to MiBand : $result")
            }, { throwable ->
                Timber.e(throwable)
                handleAuthentication()
            })
        )

    }

    private fun handleBatteryUpdate() = disposables.add(
        miBand.batteryInfo
            .subscribe({ batteryInfo ->
                setMiBandBatteryInfo(this, batteryInfo)
                Timber.d(batteryInfo.toString())
            }, { throwable -> Timber.e(throwable, "getBatteryInfo fail") }
            )
    )


    private fun handleAlert() {
        GlobalScope.launch(Dispatchers.IO) {
            actionStartVibration()
            actionStopVibration()
        }

        Intent(this, LockingService::class.java).also {
            it.action = LockingServiceActions.START.name
            startForegroundService(it)
        }

        stopService()
    }

    //Communication action handlers

    private fun actionConnect(intent: Intent?) {

        //val handler = Handler()
        val device = intent!!.getParcelableExtra<BluetoothDevice>("device")

        val d = miBand.connect(device!!)
            .subscribe({ result ->
                Timber.d("Connect onNext: $result")
                if (!result) {
                    actionConnect(intent)
                }
                actionSetPairingListener()
                handleAuthentication()
            }, { throwable ->
                throwable.printStackTrace()
                Timber.e(throwable)
            })
        disposables.add(d)
    }


    private fun actionSetPairingListener() = miBand.setPairingListener { data ->
        Timber.d("Pairing listener received response")
        when {
            //Confirmation of receiving key from phone
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x01, 0x01)) -> {
                //TODO request random number from band
                disposables.add(
                    miBand.requestRandomNumber().subscribe({ result ->
                        Timber.d("Sent random number request to MiBand : $result")
                    }, { throwable ->
                        Timber.e(throwable)
                    })
                )
            }
            //Error in receiving key from phone
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x01, 0x04)) -> {
                Timber.e("Sending pairing key failed")
            }
            //Received random number from band
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x02, 0x01)) -> {
                val randomNumber = data.sliceArray(3 until data.size)
                //TODO send encrypted random number
                disposables.add(
                    miBand.sendEncryptedNumber(randomNumber).subscribe({ result ->
                        Timber.d("Sent encrypted number to MiBand : $result")
                    }, { throwable ->
                        Timber.e(throwable)
                    })
                )
            }
            //Error in receiving random number from band
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x02, 0x04)) -> {
                Timber.e("Requesting random number failed")
            }
            //Successfully paired
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x03, 0x01)) -> {
                Timber.d("Pairing successful")
                //TODO paired
                setMiBandPaired(this, true)
                startService()
            }
            else -> {
                setMiBandPaired(this, false)
                Toast.makeText(
                    this@DataGatheringService,
                    "Pairing failed. Try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun actionSetHeartRateNotifyListener() =
        miBand.setHeartRateScanListenerMiBand2(object : HeartRateNotifyListener {
            override fun onNotify(heartRate: Int) {
                Timber.d("heart rate: $heartRate")

                GlobalScope.launch(Dispatchers.IO) {
                    heartRateRepository.insertHeartRateSample(
                        HeartRate(
                            0,
                            Calendar.getInstance(),
                            heartRate
                        )
                    )
                }
            }
        })


    private fun actionSetRealtimeStepsNotifyListener() =
        miBand.setRealtimeStepsNotifyListener(object : RealtimeStepsNotifyListener {
            override fun onNotify(steps: Int) {
                Timber.d("RealtimeStepsNotifyListener:$steps")

                GlobalScope.launch(Dispatchers.IO) {
                    stepRepository.insertBandStepSample(BandStep(0, Calendar.getInstance(), steps))
                }
            }
        })

    private fun actionSetSensorDataNotifyListener() = miBand.setSensorDataNotifyListener { data ->
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        var i = 0

        val index = data[i++].toInt() and 0xFF or (data[i++].toInt() and 0xFF shl 8)
        val d1 = data[i++].toInt() and 0xFF or (data[i++].toInt() and 0xFF shl 8)
        val d2 = data[i++].toInt() and 0xFF or (data[i++].toInt() and 0xFF shl 8)
        val d3 = data[i++].toInt() and 0xFF or (data[i++].toInt() and 0xFF shl 8)

        Timber.d("$index , $d1 , $d2 , $d3")

        GlobalScope.launch(Dispatchers.IO) {
            sensorDataRepository.insertSensorDataSample(
                SensorData(
                    0,
                    Calendar.getInstance(),
                    d1,
                    d2,
                    d3
                )
            )
        }
    }


    private fun actionStartHeartRateScan() =
        disposables.add(miBand.startRealTimeHeartRateScan().delay(1, TimeUnit.SECONDS).subscribe(
            { result ->
                Timber.d("Scan result: $result")
            }, { throwable ->
                throwable.printStackTrace()
                Timber.e(throwable)
            })
        )


    private fun actionStartVibration() = disposables.add(
        miBand.startVibration(VibrationMode.VIBRATION_WITHOUT_LED)
            .subscribe { Timber.d("Vibration started") }
    )


    private fun actionStopVibration() {
        val d = miBand.stopVibration()
            .subscribe { Timber.d("Vibration stopped") }
        disposables.add(d)
    }

    private fun actionEnableSensorDataNotify() =
        disposables.add(
            miBand.enableSensorDataNotify().delaySubscription(1, TimeUnit.SECONDS).subscribe()
        )

    private fun actionStartMeasuringSensorData() = disposables.add(
        miBand.startMeasuringSensorData().delaySubscription(2, TimeUnit.SECONDS).subscribe()
    )

    //Step counter sensor callbacks

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                var sensorSteps = event.values[0].toInt()
                var currentOffset = getStepsOffset(this)

                //Configure offset on first recording (step counter count steps from last reboot)
                if (currentOffset == -1) {
                    setStepsOffset(this, sensorSteps)
                    currentOffset = sensorSteps
                }

                GlobalScope.launch(Dispatchers.IO) {
                    delay(100)
                    val latest: PhoneStep? = stepRepository.getLatestPhoneStepSample()
                    val newTimestamp = Calendar.getInstance()

                    //reboot -> clear offset and record latest sample to fix current steps
                    if (currentOffset > sensorSteps) {
                        setStepsOffset(
                            this@DataGatheringService,
                            0
                        )
                        setStepsOffsetFix(
                            this@DataGatheringService,
                            latest?.stepCount ?: 0
                        )
                    }

                    //Day changed -> start counting steps from zero
                    if (latest != null) {
                        if (latest.timestamp.get(Calendar.DAY_OF_MONTH) != newTimestamp.get(Calendar.DAY_OF_MONTH)) {

                            //increase offset by yesterday stepCount or stepCount - offsetFix if we're still fixing reboot
                            val offsetFix =
                                if (sensorSteps < latest.stepCount) latest.stepCount - getStepsOffsetFix(
                                    this@DataGatheringService
                                ) else latest.stepCount

                            setStepsOffset(
                                this@DataGatheringService,
                                offsetFix + currentOffset
                            )

                        } else {

                            //fix step number -> add steps from sample before reboot
                            if (sensorSteps < latest.stepCount) {
                                sensorSteps += getStepsOffsetFix(this@DataGatheringService)
                            }
                        }
                    }

                    stepRepository.insertPhoneStepSample(
                        PhoneStep(
                            0,
                            newTimestamp,
                            sensorSteps - getStepsOffset(this@DataGatheringService)
                        )
                    )

                }
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "MI BAND SERVICE CHANNEL"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId,
            "Mi Band Service notifications channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "Mi Band Service channel"
            it.enableLights(true)
            it.lightColor = Color.WHITE
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
            .setContentTitle("Monitoring your device")
            .setContentText("Tap to open app")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.outline_track_changes_black_18dp)
            .setTicker(";)")
            .build()
    }

}
