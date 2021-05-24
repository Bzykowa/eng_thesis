package com.example.lockband.services

import android.app.*
import android.bluetooth.BluetoothAdapter
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
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.data.MiBandServiceActions
import com.example.lockband.data.room.entities.BandStep
import com.example.lockband.data.room.entities.HeartRate
import com.example.lockband.data.room.entities.PhoneStep
import com.example.lockband.data.room.repos.HeartRateRepository
import com.example.lockband.data.room.repos.StepRepository
import com.example.lockband.miband3.MiBand
import com.example.lockband.miband3.listeners.HeartRateNotifyListener
import com.example.lockband.miband3.listeners.RealtimeStepsNotifyListener
import com.example.lockband.miband3.model.BatteryInfo
import com.example.lockband.miband3.model.Protocol
import com.example.lockband.utils.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * Service that is responsible for communicating with MiBand 3
 */
@AndroidEntryPoint
class MiBandService : Service(), SensorEventListener {

    @Inject
    lateinit var stepRepository: StepRepository

    @Inject
    lateinit var heartRateRepository: HeartRateRepository


    private var miBand = MiBand(this)
    private val disposables = CompositeDisposable()

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var btTurnedOff = false

    /**
     * BroadcastReceiver responsible for refreshing Battery data
     */
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleBatteryUpdate()
        }
    }

    /**
     * BroadcastReceiver responsible for initiating reconnection
     */
    private val reconnectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.d("Reconnection attempt received")
            handleReconnection()
        }
    }

    /**
     * BroadcastReceiver responsible for monitoring state of BTAdapter. Reconnects if BTAdapter is
     * turned on again after period of time shorter than BT_TIMEOUT, otherwise it stops MiBandService
     */
    private val btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    btTurnedOff = true
                    GlobalScope.launch(Dispatchers.Default) {
                        delay(BT_TIMEOUT)
                        if (btTurnedOff) {
                            stopService()
                        }
                    }
                }
                BluetoothAdapter.STATE_TURNING_OFF -> Timber.d("BT Turning off")
                BluetoothAdapter.STATE_ON -> {
                    btTurnedOff = false
                    actionConnect(currentIntent)
                }
                BluetoothAdapter.STATE_TURNING_ON -> Timber.d("BT turning on")
            }
        }

    }

    private val batteryIntentFilter = IntentFilter(MiBandServiceActions.BATTERY.name)
    private val reconnectIntentFilter = IntentFilter(MiBandServiceActions.RECONNECT.name)
    private val btIntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

    private lateinit var currentIntent: Intent


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Service control  ****************************************************************************
     */

    /**
     * Parses intents and launches appropriate functions based on them. Also registers necessary
     * BroadcastReceivers
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                MiBandServiceActions.PAIR.name -> {
                    currentIntent = intent
                    actionConnect(intent)
                }
                MiBandServiceActions.START.name -> {
                    currentIntent = intent
                    actionConnect(intent)
                }
                MiBandServiceActions.STOP.name -> stopService()
                else -> Timber.e("This should never happen. No action in the received intent")
            }
        } else {
            Timber.d(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        registerReceiver(reconnectReceiver, reconnectIntentFilter)
        registerReceiver(btReceiver, btIntentFilter)
        // restarted if the system kills the service
        return START_STICKY
    }

    /**
     * Registers Step Counter Sensor and creates notification
     */
    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        Timber.d("The Mi Band communication service has been created")
        val notification = createNotification()
        startForeground(2, notification)
    }

    /**
     * Unregisters Bluetooth BroadcastReceivers
     */
    override fun onDestroy() {
        unregisterReceiver(btReceiver)
        unregisterReceiver(reconnectReceiver)
        super.onDestroy()
        Timber.d("The Mi Band communication service has been destroyed")
    }

    /**
     * Restarts Service if it is killed by something
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent =
            Intent(applicationContext, MiBandService::class.java).also {
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

    /**
     * Keeps MiBandService alive and periodically checks for inconsistencies in gathered data
     */
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

        var beforeDate = Calendar.getInstance()

        GlobalScope.launch(Dispatchers.IO) {

            while (isServiceStarted) {

                delay(90000)
                Timber.d("Mi Band Service working...")

                val nowDate = Calendar.getInstance()
                val stepPhoneSamples = stepRepository.getPhoneStepSamplesBetween(beforeDate, nowDate)
                val stepBandSamples = stepRepository.getBandStepSamplesBetween(beforeDate, nowDate)
                val stepPhoneAmplitude = when{
                    stepPhoneSamples.last().offset > stepPhoneSamples.first().offset -> stepPhoneSamples.last().offset - stepPhoneSamples.first().offset
                    stepPhoneSamples.last().offset < stepPhoneSamples.first().offset && stepPhoneSamples.last().stepCount > stepPhoneSamples.first().stepCount -> stepPhoneSamples.last().stepCount - stepPhoneSamples.first().stepCount
                    else -> stepPhoneSamples.first().offset
                }
                val stepBandAmplitude = when {
                    stepBandSamples.last().stepCount > stepBandSamples.first().stepCount -> stepBandSamples.last().stepCount - stepBandSamples.first().stepCount
                    else -> stepBandSamples.last().stepCount + stepBandSamples.first().stepCount
                }

                when {
                    heartRateRepository.getHeartRateSamplesBetween(beforeDate, nowDate).isEmpty() -> {
                        Timber.d("No hr registered in last 90 seconds. Band off hand. Lock the phone!")
                        Intent(this@MiBandService, LockingService::class.java).also {
                            it.action = LockingServiceActions.START.name
                            startForegroundService(it)
                        }
                        stopService()
                    }
                    abs(stepPhoneAmplitude-stepBandAmplitude) > MAX_STEPS_DIFF -> {
                        Timber.d("Steps growth isn't similar between MiBand and phone. Lockdown!")
                        Intent(this@MiBandService, LockingService::class.java).also {
                            it.action = LockingServiceActions.START.name
                            startForegroundService(it)
                        }
                    }
                    else -> {
                        beforeDate = nowDate
                    }
                }
            }

        }

    }

    /**
     * Closes MiBandService when suspicious situations are recorded. Releases all Disposables and unregisters
     * Step Counter Sensor and BroadcastReceiver responsible for Battery updates
     */
    private fun stopService() {
        Timber.d("Stopping the mi band foreground service")
        isServiceStarted = false

        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            miBand.disconnectCompletely()

            unregisterReceiver(batteryReceiver)

            disposables.clear()

            sensorManager.unregisterListener(this)

            stopForeground(true)
            stopSelf()

        } catch (e: Exception) {
            Timber.d("Mi Band Service stopped without being started: ${e.message}")
        }
        setMiBandServiceState(this, MiBandServiceState.STOPPED)
    }

    /**
     * Communication methods    ********************************************************************
     */

    /**
     * Launches "After connection" sequence which updates MiBand settings and then starts monitoring
     * data based on samples from last 90 seconds
     */
    private fun handleDeviceSetup() {
        miBand.removeAuthenticationListener()
        enableDataNotifications()

        GlobalScope.launch(Dispatchers.IO) {
            delay(OP_TIMEOUT)
            actionReadSerialNumber()
            delay(OP_TIMEOUT)
            actionReadHardwareRevision()
            delay(OP_TIMEOUT)
            actionReadSoftwareRevision()
            delay(OP_TIMEOUT)
            handleBatteryUpdate()
            delay(OP_TIMEOUT)
            miBand.setEnglishLanguage()
            delay(OP_TIMEOUT)
            miBand.disableScreenUnlock()
            delay(OP_TIMEOUT)
            miBand.disableNightMode()
            delay(OP_TIMEOUT)
            miBand.setDateFormat()
            delay(OP_TIMEOUT)
            miBand.setDateDisplay()
            delay(OP_TIMEOUT)
            miBand.setTimeFormat()
            delay(OP_TIMEOUT)
            setUserInfo()
            delay(OP_TIMEOUT)
            miBand.setMetricUnits()
            delay(OP_TIMEOUT)
            setUserInfoListener()
            delay(OP_TIMEOUT)
            miBand.setWearLocation()
            delay(OP_TIMEOUT)
            setFitnessGoal()
            delay(OP_TIMEOUT)
            miBand.setDisplayItems()
            delay(OP_TIMEOUT)
            miBand.disableDND()
            delay(OP_TIMEOUT)
            miBand.disableRotateWristToSwitchInfo()
            delay(OP_TIMEOUT)
            miBand.disableLiftWristToActivateDisplay()
            delay(OP_TIMEOUT)
            miBand.enableDisplayCaller()
            delay(OP_TIMEOUT)
            miBand.disableGoalNotification()
            delay(OP_TIMEOUT)
            miBand.disableInactivityWarnings()
            delay(OP_TIMEOUT)
            setEnableHeartRateSleepSupport()
            delay(OP_TIMEOUT)
            miBand.enableDisconnectNotification()
            delay(OP_TIMEOUT)
            miBand.enableBTConnectedAdvertisement()
            delay(OP_TIMEOUT)
            setHeartRateMeasureInterval()
            delay(OP_TIMEOUT)
            miBand.requestAlarms()
            delay(OP_TIMEOUT)
            //Register BroadcastReceiver for battery update
            registerReceiver(batteryReceiver, batteryIntentFilter)
            //set up listeners for band data scans
            actionSetHeartRateNotifyListener()
            delay(OP_TIMEOUT)
            actionSetRealtimeStepsNotifyListener()
            delay(MONITORING_TIMEOUT)
            startService()
        }
        currentIntent.action = MiBandServiceActions.START.name
    }

    /**
     * Starts pairing sequence
     */
    private fun handleAuthentication() {
        Timber.d("Starting to authenticate")

        disposables.add(
            miBand.initializePairing().delaySubscription(2, TimeUnit.SECONDS)
                .subscribe({ result ->
                    Timber.d("Sent key to MiBand : $result")
                }, { throwable ->
                    Timber.e(throwable)
                    handleAuthentication()
                })
        )

    }

    /**
     * Requests Battery data update from MiBand
     */
    private fun handleBatteryUpdate() = disposables.add(
        miBand.batteryInfo
            .subscribe({ batteryInfo ->
                setMiBandBatteryInfo(this, batteryInfo)
                Timber.d(batteryInfo.toString())
            }, { throwable -> Timber.e(throwable, "getBatteryInfo fail") }
            )
    )

    /**
     * Sets placeholder information about user
     */
    private fun setUserInfo() = disposables.add(
        miBand.setUserInfo(user).subscribe({
            Timber.d("User info set")
        }, {
            Timber.e(it)
        })
    )

    /**
     * Sets placeholder steps goal of stepGoal steps
     */
    private fun setFitnessGoal() = disposables.add(
        miBand.setFitnessGoal().subscribe({
            Timber.d("Set fitness goal to $stepGoal")
        }, {
            Timber.e(it)
        })
    )


    /**
     * Attempts reconnection when connection drops (MAX_RECONNECTIONS tries allowed before locking phone)
     */
    private fun handleReconnection() {
        val d = miBand.reconnect().subscribe({ result ->
            Timber.d("Reconnect onNext: $result")
            if (!result) {
                handleReconnection()
            } else {
                actionSetAuthenticationListener()
                GlobalScope.launch(Dispatchers.IO) {
                    delay(DEFAULT_TIMEOUT / 2)
                    miBand.requestRandomNumber()
                }
            }
        }, { throwable ->
            throwable.printStackTrace()
            Timber.e(throwable)
        })
        disposables.add(d)
    }

    /**
     * Initiates connection and authentication sequence
     *
     * @param intent Gives information about device and if it's pairing time
     */
    private fun actionConnect(intent: Intent?) {

        val device = intent!!.getParcelableExtra<BluetoothDevice>("device")

        val d = miBand.connect(device!!)
            .subscribe({ result ->
                Timber.d("Connect onNext: $result")
                when {
                    !result -> {
                        actionConnect(intent)
                    }
                    //Pairing so we need to send auth key to Mi Band
                    intent.action == MiBandServiceActions.PAIR.name -> {
                        actionRequestMtu(512)
                        actionSetAuthenticationListener()
                        handleAuthentication()
                    }
                    //Mi Band paired. Skip sending key.
                    else -> {
                        actionSetAuthenticationListener()
                        GlobalScope.launch {
                            delay(DEFAULT_TIMEOUT / 2)
                            miBand.requestRandomNumber()
                        }
                    }
                }
            }, { throwable ->
                throwable.printStackTrace()
                Timber.e(throwable)
            })
        disposables.add(d)
    }

    /**
     * Request special MTU value
     *
     * @param mtu MTU to set
     */
    private fun actionRequestMtu(mtu: Int) {
        val d = miBand.requestMtu(mtu).delaySubscription(1, TimeUnit.SECONDS).subscribe({
            Timber.d("mtu :$it")
        }, {
            Timber.e(it)
        })
        disposables.add(d)
    }

    /**
     * Sets up listener to receive response from UUID_CHAR_USER_INFO characteristic
     */
    private fun setUserInfoListener() = miBand.setUserInfoListener { data ->
        Timber.d("Received response from UserInfo characteristic")
        if (data.isEmpty()) {
            Timber.d("Set up wear location")
            miBand.removeUserInfoListener()
        }
    }


    /**
     * Sets up listener for authentication sequence on UUID_CHAR_PAIR characteristic
     */
    private fun actionSetAuthenticationListener() = miBand.setAuthenticationListener { data ->
        Timber.d("Pairing listener received response")
        when {
            //Confirmation of receiving key from phone
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x01, 0x01)) -> {
                //request random number from band
                miBand.requestRandomNumber()
            }
            //Error in receiving key from phone
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x01, 0x04)) -> {
                Timber.e("Sending authentication key failed")
            }
            //Received random number from band
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x02, 0x01)) -> {
                val randomNumber = data.sliceArray(3 until 19)
                //send encrypted random number
                miBand.sendEncryptedNumber(randomNumber)
                if (currentIntent.action != MiBandServiceActions.START.name) {
                    setTime()
                }
            }
            //Error in receiving random number from band
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x02, 0x04)) -> {
                Timber.e("Requesting random number failed")
            }
            //Successfully paired
            data.sliceArray(0..2).contentEquals(byteArrayOf(0x10, 0x03, 0x01)) -> {
                Timber.d("Authentication successful")
                //set paired and move on to next stage of setup
                setMiBandPaired(this, true)
                handleDeviceSetup()
            }
            else -> {
                setMiBandPaired(this, false)
                Toast.makeText(
                    this@MiBandService,
                    "Authentication failed. Try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Sets up listener for MiBand settings on UUID_CHAR_CONTROL_POINT characteristic
     */
    private fun setBandControlListener() = miBand.setBandControlListener { data ->
        Timber.d("Band control characteristic received response")
        when {
            data.first() == 0x10.toByte() && data.last() == 0x01.toByte() -> {
                Timber.d("Success -> ${Protocol.actions[printHexBinary(data.sliceArray(1 until data.lastIndex))]}")
            }
            data.first() == 0x10.toByte() && data.last() != 0x01.toByte() -> {
                Timber.d("Other response -> ${Protocol.actions[printHexBinary(data.sliceArray(1 until data.lastIndex))]}")
            }
            else -> {
                Timber.e("Communication error -> response: $data")
            }
        }
    }

    /**
     * Sets up current date and time on MiBand
     */
    private fun setTime() = disposables.add(
        miBand.setCurrentTime().subscribe(
            {
                Timber.d("Set current time on band")
            },
            {
                Timber.e(it)
            }
        )
    )

    /**
     * Sets up listeners for MiBand settings, battery information and registered events
     */
    private fun enableDataNotifications() {
        GlobalScope.launch(Dispatchers.IO) {
            setBandControlListener()
            delay(OP_TIMEOUT)
            setBatteryInfoListener()
            delay(OP_TIMEOUT)
            setBandEventsListener()
        }
    }

    /**
     * Sets up listener for battery information update on UUID_CHAR_BATTERY characteristic
     */
    private fun setBatteryInfoListener() = miBand.setBatteryInfoListener { data ->
        val batteryInfo = BatteryInfo.fromByteData(data)
        setMiBandBatteryInfo(this, batteryInfo)
        Timber.d(batteryInfo.toString())
    }

    /**
     * Sets up listener for catching falling asleep and taking off band on UUID_CHAR_DEVICEEVENT
     * characteristic. Registering events takes sometimes a lot of time so taking off band is also
     * caught by lack of or wrong HR data.
     */
    private fun setBandEventsListener() = miBand.setBandEventsListener { data ->
        when (data.first()) {
            Protocol.FELL_ASLEEP -> {
                Timber.d("Registered sleeping")
                Intent(this, LockingService::class.java).also {
                    it.action = LockingServiceActions.START.name
                    startForegroundService(it)
                }
                stopService()
            }
            Protocol.START_NONWEAR -> {
                Timber.d("Took off band")
                Intent(this, LockingService::class.java).also {
                    it.action = LockingServiceActions.START.name
                    startForegroundService(it)
                }
                stopService()
            }
        }
        Timber.d(printHexBinary(data))
    }

    /**
     * Requests serial number from MiBand
     */
    private fun actionReadSerialNumber() = disposables.add(
        miBand.readSerialNumber().delaySubscription(1, TimeUnit.SECONDS).subscribe({
            setMiBandSerialNumber(this@MiBandService, it)
            Timber.d("Serial number: $it")
        }, {
            Timber.e(it)
        })
    )

    /**
     * Requests hardware version from MiBand
     */
    private fun actionReadHardwareRevision() = disposables.add(
        miBand.readHardwareRevision().delaySubscription(1, TimeUnit.SECONDS).subscribe({
            setMiBandHardwareRevision(this@MiBandService, it)
            Timber.d("Hardware revision: $it")
        }, {
            Timber.e(it)
        })
    )

    /**
     * Requests software verison form MiBand
     */
    private fun actionReadSoftwareRevision() = disposables.add(
        miBand.readSoftwareRevision().delaySubscription(1, TimeUnit.SECONDS).subscribe({
            setMiBandSoftwareRevision(this@MiBandService, it)
            Timber.d("Software revision: $it")
        }, {
            Timber.e(it)
        })
    )

    /**
     * Sets up listener for heart rate on UUID_CHAR_HEARTRATE characteristic
     */
    private fun actionSetHeartRateNotifyListener() =
        miBand.setHeartRateScanListenerMiBand2(object : HeartRateNotifyListener {
            override fun onNotify(heartRate: Int) {
                Timber.d("heart rate: $heartRate")

                if (heartRate == 0) {
                    Timber.d("MiBand not on hand! Lockdown tim!")
                    Intent(this@MiBandService, LockingService::class.java).also {
                        it.action = LockingServiceActions.START.name
                        startForegroundService(it)
                    }
                    stopService()
                } else {
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
            }
        })

    /**
     * Sets up listener for steps from MiBand on UUID_CHAR_REALTIME_STEPS characteristic
     */
    private fun actionSetRealtimeStepsNotifyListener() =
        miBand.setRealtimeStepsNotifyListener(object : RealtimeStepsNotifyListener {
            override fun onNotify(steps: Int) {
                Timber.d("RealtimeStepsNotifyListener: $steps")

                GlobalScope.launch(Dispatchers.IO) {
                    val latest = stepRepository.getLatestBandStepSample()
                    if (steps != latest.stepCount) {
                        stepRepository.insertBandStepSample(
                            BandStep(
                                0,
                                Calendar.getInstance(),
                                steps
                            )
                        )
                    }
                }
            }
        })

    /**
     * Requests better sleep recognition from MiBand
     */
    private fun setEnableHeartRateSleepSupport() =
        disposables.add(miBand.enableHeartRateSleepSupport().delaySubscription(1, TimeUnit.SECONDS)
            .subscribe(
                { result ->
                    Timber.d("Sleep support: $result")
                }, { throwable ->
                    throwable.printStackTrace()
                    Timber.e(throwable)
                })
        )


    /**
     * Requests lowest possible heart rate measurement interval from MiBand
     */
    private fun setHeartRateMeasureInterval() =
        disposables.add(miBand.setHeartRateMeasureInterval().delaySubscription(1, TimeUnit.SECONDS)
            .subscribe(
                { result ->
                    Timber.d("Scan result: $result")
                }, { throwable ->
                    throwable.printStackTrace()
                    Timber.e(throwable)
                })
        )

    /**
     * Step sensor data callbacks   ****************************************************************
     */

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val sensorSteps = event.values[0].toInt()

                GlobalScope.launch(Dispatchers.IO) {
                    delay(OP_TIMEOUT)
                    val latest = stepRepository.getLatestPhoneStepSample()
                    val newTimestamp = Calendar.getInstance()
                    val newStepCount = when {
                        //First time -> record only offset
                        latest.offset == 0 && latest.stepCount == 0 -> {
                            0
                        }
                        //reboot -> Offset was zeroed just add new value to old one -> offset = sensorSteps, stepCount = latest.stepCount + sensorSteps
                        latest.offset > sensorSteps -> {
                            latest.stepCount + sensorSteps
                        }
                        //Day changed -> start counting steps from zero -> offset = sensorSteps, stepCount = sensorSteps - latest.offset
                        latest.timestamp.get(Calendar.DAY_OF_MONTH) != newTimestamp.get(Calendar.DAY_OF_MONTH) -> {
                            sensorSteps - latest.offset
                        }
                        //Normal assignment -> last value + offset difference
                        else -> {
                            latest.stepCount + (sensorSteps - latest.offset)
                        }
                    }

                    stepRepository.insertPhoneStepSample(
                        PhoneStep(
                            0,
                            newTimestamp,
                            newStepCount,
                            sensorSteps
                        )
                    )

                }
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    /**
     * Service notification ************************************************************************
     */

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
