package com.khmelenko.lab.miband

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.core.content.ContextCompat.startForegroundService
import com.example.lockband.data.LockingServiceActions
import com.example.lockband.services.LockingService
import com.example.lockband.utils.*
import com.khmelenko.lab.miband.listeners.HeartRateNotifyListener
import com.khmelenko.lab.miband.listeners.RealtimeStepsNotifyListener
import com.khmelenko.lab.miband.model.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Main class for interacting with MiBand

 * @author Dmytro Khmelenko
 */
class MiBand(private val context: Context) : BluetoothListener {

    private val bluetoothIo: BluetoothIO = BluetoothIO(this)

    private var connectionSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var rssiSubject: PublishSubject<Int> = PublishSubject.create()
    private var batteryInfoSubject: PublishSubject<BatteryInfo> = PublishSubject.create()
    private var pairInitSubject: PublishSubject<Void> = PublishSubject.create()
    private var requestNumberSubject: PublishSubject<Void> = PublishSubject.create()
    private var sendEncNumSubject: PublishSubject<Void> = PublishSubject.create()
    private var pairRequested: Boolean = false
    private var startVibrationSubject: PublishSubject<Void> = PublishSubject.create()
    private var stopVibrationSubject: PublishSubject<Void> = PublishSubject.create()
    private var sensorNotificationSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var sensorDataSubject: PublishSubject<Void> = PublishSubject.create()
    private var realtimeNotificationSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var ledColorSubject: PublishSubject<LedColor> = PublishSubject.create()
    private var userInfoSubject: PublishSubject<Void> = PublishSubject.create()
    private var heartRateSubject: PublishSubject<Void> = PublishSubject.create()
    private var mtuSubject: PublishSubject<Int> = PublishSubject.create()
    private var timeSubject: PublishSubject<Void> = PublishSubject.create()
    private var serialNumberSubject: PublishSubject<String> = PublishSubject.create()
    private var hardwareRevisionSubject: PublishSubject<String> = PublishSubject.create()
    private var softwareRevisionSubject: PublishSubject<String> = PublishSubject.create()

    val device: BluetoothDevice?
        get() = bluetoothIo.getConnectedDevice()

    /**
     * Starts scanning for devices

     * @return An Observable which emits ScanResult
     */
    fun startScan(): Observable<ScanResult> {
        val handler = Handler()

        return Observable.create<ScanResult> { subscriber ->
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null) {
                val scanner = adapter.bluetoothLeScanner
                if (scanner != null) {
                    scanner.startScan(
                        listOf(
                            ScanFilter.Builder().setDeviceName("Mi Band 3").build()
                        ),
                        ScanSettings.Builder()
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build(),
                        getScanCallback(subscriber)
                    )
                    handler.postDelayed({
                        stopScan()
                    }, SCAN_TIMEOUT)
                } else {
                    Timber.d("BluetoothLeScanner is null")
                    subscriber.onError(NullPointerException("BluetoothLeScanner is null"))
                }
            } else {
                Timber.d("BluetoothAdapter is null")
                subscriber.onError(NullPointerException("BluetoothLeScanner is null"))
            }
        }
    }

    /**
     * Stops scanning for devices

     * @return An Observable which emits ScanResult
     */
    fun stopScan(): Observable<ScanResult> {
        return Observable.create<ScanResult> { subscriber ->
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null) {
                val scanner = adapter.bluetoothLeScanner
                if (scanner != null) {
                    scanner.stopScan(getScanCallback(subscriber))
                } else {
                    Timber.d("BluetoothLeScanner is null")
                    subscriber.onError(NullPointerException("BluetoothLeScanner is null"))
                }
            } else {
                Timber.d("BluetoothAdapter is null")
                subscriber.onError(NullPointerException("BluetoothLeScanner is null"))
            }
        }
    }

    /**
     * Creates [ScanCallback] instance

     * @param subscriber Subscriber
     * *
     * @return ScanCallback instance
     */
    private fun getScanCallback(subscriber: ObservableEmitter<in ScanResult>): ScanCallback {
        return object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                subscriber.onError(Exception("Scan failed, error code $errorCode"))
            }

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                subscriber.onNext(result)
                subscriber.onComplete()
            }
        }
    }

    /**
     * Starts connection process to the device

     * @param device Device to connect
     */
    fun connect(device: BluetoothDevice): Observable<Boolean> {
        return Observable.create<Boolean> { subscriber ->
            Timber.d("MiBand connect() invoked")
            connectionSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.connect(context, device)
        }
    }

    /**
     * Initializes device pairing (sends key to MiBand)
     */
    fun initializePairing(): Observable<Void> {
        return Observable.create<Void> {
            pairRequested = true
            Timber.d("MiBand pairing invoked")
            pairInitSubject.subscribe(ObserverWrapper(it))
            Timber.d("${Protocol.SEND_KEY.asList()}")
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MIBAND2,
                Profile.UUID_CHAR_PAIR,
                Protocol.SEND_KEY
            )
        }
    }

    /**
     * Requests random key from MiBand
     */
    fun requestRandomNumber(): Observable<Void> {
        return Observable.create<Void> {
            Timber.d("Request random number from MiBand")
            requestNumberSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MIBAND2,
                Profile.UUID_CHAR_PAIR,
                Protocol.REQ_RAND_NUMBER
            )
        }
    }

    /**
     * Send encrypted number to band
     */
    fun sendEncryptedNumber(num: ByteArray): Observable<Void> {
        return Observable.create<Void> {
            Timber.d("Send encrypted number to MiBand")

            val encryptedNum = encryptAES(num, KEY)
            val message = Protocol.SEND_ENC_NUMBER + encryptedNum

            sendEncNumSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MIBAND2,
                Profile.UUID_CHAR_PAIR,
                message
            )
        }
    }


    /**
     * Reads Received Signal Strength Indication (RSSI)
     */
    fun readRssi(): Observable<Int> {
        return Observable.create<Int> { subscriber ->
            rssiSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.readRssi()
        }
    }

    fun requestMtu(mtu: Int): Observable<Int> {
        return Observable.create<Int> {
            mtuSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.requestMtu(mtu)
        }
    }

    /**
     * Send current time to band
     */
    fun setCurrentTime() : Observable<Void> {
        val now: GregorianCalendar = CalendarConversions.createCalendar()
        val bytes: ByteArray = getTimeBytes(now, TimeUnit.SECONDS)!!

        return Observable.create<Void>{
            timeSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.writeCharacteristic(Profile.UUID_SERVICE_MILI,Profile.UUID_CHAR_DATA_TIME,bytes)
        }
    }

    /**
     * Requests battery info

     * @return Battery info instance
     */
    val batteryInfo: Observable<BatteryInfo>
        get() = Observable.create<BatteryInfo> { subscriber ->
            batteryInfoSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.readCharacteristic(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_BATTERY)
        }

    /**
     * Set battery info listener
     *
     * @param listener Battery listener
     */
    fun setBatteryInfoListener(listener: (ByteArray) -> Unit) {
        Timber.d("Setting up battery info listener")
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_BATTERY,
            listener
        )
    }

    /**
     * Set band control listener
     *
     * @param listener Control listener
     */
    fun setBandControlListener(listener: (ByteArray) -> Unit) {
        Timber.d("Setting up band control listener")
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_CONTROL_POINT,
            listener
        )
    }

    /**
     * Set band events listener
     *
     * @param listener Events listener
     */
    fun setBandEventsListener(listener: (ByteArray) -> Unit) {
        Timber.d("Setting up band events listener")
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_DEVICEEVENT,
            listener
        )
    }


    /**
     * Read band serial number
     */
    fun readSerialNumber(): Observable<String> {
        return Observable.create<String> { subscriber ->
            serialNumberSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_SERIAL_NUMBER)
        }
    }

    /**
     * Read band hardware version
     */
    fun readHardwareRevision() : Observable<String> {
        return Observable.create<String>{
            hardwareRevisionSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_HARDWARE_REVISION)
        }
    }

    /**
     * Read band software version
     */
    fun readSoftwareRevision() : Observable<String> {
        return Observable.create<String>{
            softwareRevisionSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_SOFTWARE_REVISION)
        }
    }

    /**
     * Requests starting vibration
     */
    fun startVibration(mode: VibrationMode): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            val protocol = when (mode) {
                VibrationMode.VIBRATION_WITH_LED -> Protocol.VIBRATION_WITH_LED
                VibrationMode.VIBRATION_10_TIMES_WITH_LED -> Protocol.VIBRATION_10_TIMES_WITH_LED
                VibrationMode.VIBRATION_WITHOUT_LED -> Protocol.VIBRATION_WITHOUT_LED
            }
            startVibrationSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_VIBRATION,
                Profile.UUID_CHAR_VIBRATION,
                protocol
            )
        }
    }

    /**
     * Requests stopping vibration
     */
    fun stopVibration(): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            stopVibrationSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION,
                Protocol.STOP_VIBRATION
            )
        }
    }


    /**
     * Enables sensor notifications on hr and accelerometer (010319)
     */
    fun enableSensorDataNotify(): Observable<Boolean> {
        return Observable.create<Boolean> { subscriber ->
            sensorNotificationSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI,
                Profile.UUID_CHAR_SENSOR_DATA,
                Protocol.ENABLE_SENSOR_DATA_NOTIFY
            )
        }
    }

    fun startSensorMeasurement(): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            sensorDataSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI,
                Profile.UUID_CHAR_SENSOR_DATA, byteArrayOf(2)
            )
        }
    }

    fun disableSensorMeasurement(): Observable<Void> {
        return Observable.create<Void>{
            sensorDataSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI,
                Profile.UUID_CHAR_SENSOR_DATA, byteArrayOf(2)
            )
        }

    }

    /**
     * Disables sensor notifications
     */
    fun disableSensorDataNotify(): Observable<Boolean> {
        return Observable.create<Boolean> { subscriber ->
            sensorNotificationSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA,
                Protocol.DISABLE_SENSOR_DATA_NOTIFY
            )
        }
    }


    /**
     * Sets sensor data notification listener

     * @param listener Notification listener
     */
    fun setSensorDataNotifyListener(listener: (ByteArray) -> Unit) {
        Timber.d("Setting up sensor listener")
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_SENSOR_DATA,
            listener
        )
    }

    /**
     * Set authentication notification listener
     *
     * @param listener Pairing listener
     */
    fun setAuthenticationListener(listener: (ByteArray) -> Unit) {
        Timber.d("Setting up pairing listener")
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MIBAND2,
            Profile.UUID_CHAR_PAIR,
            listener
        )
    }

    /**
     * Disable notifications on initial authentication
     *
     */
    fun removeAuthenticationListener() {
        Timber.d("Removing pairing listener")
        bluetoothIo.removeNotifyListener(
            Profile.UUID_SERVICE_MIBAND2,
            Profile.UUID_CHAR_PAIR
        )
    }

    fun removeSensorListener() {
        Timber.d("Removing sensor listener")
        bluetoothIo.removeNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA)
    }

    fun removeHRListeners() {
        Timber.d("Removing HR listeners")
        bluetoothIo.removeNotifyListener(
            Profile.UUID_SERVICE_HEARTRATE,
            Profile.UUID_CHAR_HEARTRATE
        )
    }


    /*
    *Unnecessary in MiBand3
    /**
     * Enables realtime steps notification
     */
    fun enableRealtimeStepsNotify(): Observable<Boolean> {
        return Observable.create<Boolean> { subscriber ->
            realtimeNotificationSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_CONTROL_POINT,
                Protocol.ENABLE_REALTIME_STEPS_NOTIFY
            )
        }
    }

    /**
     * Disables realtime steps notification
     */
    fun disableRealtimeStepsNotify(): Observable<Boolean> {
        return Observable.create<Boolean> { subscriber ->
            realtimeNotificationSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_CONTROL_POINT,
                Protocol.DISABLE_REALTIME_STEPS_NOTIFY
            )
        }
    }

     */

    //fun readSteps(): Observable<Int> {}

    /**
     * Sets realtime steps notification listener

     * @param listener Notification listener
     */
    fun setRealtimeStepsNotifyListener(listener: RealtimeStepsNotifyListener) {
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_REALTIME_STEPS
        ) { data: ByteArray ->
            Timber.d(data.contentToString())
            if (data.size == 4) {
                val steps = data[3].toInt() shl 24 or (data[2].toInt() and 0xFF shl 16) or
                        (data[1].toInt() and 0xFF shl 8) or (data[0].toInt() and 0xFF)
                listener.onNotify(steps)
            }
        }
    }

    /**
     * Sets notification listener

     * @param listener Listener
     */
    fun setNormalNotifyListener(listener: (ByteArray) -> Unit) {
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_NOTIFICATION,
            listener
        )
    }

    /**
     * Sets LED color

     * @param color Color
     */
    fun setLedColor(color: LedColor): Observable<LedColor> {
        return Observable.create<LedColor> { subscriber ->
            val protocol: ByteArray = when (color) {
                LedColor.RED -> Protocol.SET_COLOR_RED
                LedColor.BLUE -> Protocol.SET_COLOR_BLUE
                LedColor.GREEN -> Protocol.SET_COLOR_GREEN
                LedColor.ORANGE -> Protocol.SET_COLOR_ORANGE
            }
            ledColorSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI,
                Profile.UUID_CHAR_CONTROL_POINT,
                protocol
            )
        }

    }

    /**
     * Sets user info

     * @param userInfo User info
     */
    fun setUserInfo(userInfo: UserInfo): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            userInfoSubject.subscribe(ObserverWrapper(subscriber))

            val data = userInfo.getBytes(device?.address ?: "")
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI,
                Profile.UUID_CHAR_CONTROL_POINT,
                data
            )
        }
    }

    /**
     * Sets up support for better sleep recognition using hr data
     */
    fun enableHeartRateSleepSupport(): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            heartRateSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CONTROL_HEARTRATE,
                Protocol.SET_HR_SLEEP_SUPPORT
            )
        }
    }

    /**
     * Sets up automatic hr measure interval to 1 minute (shortest available)
     */
    fun setHeartRateMeasureInterval(): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            heartRateSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CONTROL_HEARTRATE,
                Protocol.SET_HR_MEASURE_INTERVAL
            )
        }
    }

    /**
     * Starts heart rate scanner real time
     */
    fun startManualHeartRateScan(): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            heartRateSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CONTROL_HEARTRATE,
                Protocol.START_HR_SCAN_MANUAL
            )
        }
    }

    fun pingHRService() {
        bluetoothIo.writeCharacteristic(
            Profile.UUID_SERVICE_HEARTRATE,
            Profile.UUID_CONTROL_HEARTRATE,
            Protocol.PING_HR_MONITOR
        )
    }

    /**
     * Stops heart rate scanner
     */
    fun disableRealTimeHeartRateScan(): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            heartRateSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CONTROL_HEARTRATE,
                Protocol.DISABLE_HEART_RATE_SCAN_AUTO
            )
        }
    }

    /**
     * Sets heart rate scanner listener
     *
     * @param listener Listener
     */
    fun setHeartRateScanListener(listener: HeartRateNotifyListener) {
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_HEARTRATE,
            Profile.UUID_CONTROL_HEARTRATE
        ) { data ->
            Timber.d(data.contentToString())
            if (data.size == 2 && data[0].toInt() == 6) {
                val heartRate = data[1].toInt() and 0xFF
                listener.onNotify(heartRate)
            }
        }
    }

    /**
     * Sets heart rate scanner listener for Xiaomi MiBand 2
     *
     * @param listener Listener
     */
    fun setHeartRateScanListenerMiBand2(listener: HeartRateNotifyListener) {
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_HEARTRATE,
            Profile.UUID_CHAR_HEARTRATE
        ) { data ->
            Timber.d(data.contentToString())
            if (data.size == 2 && data[0].toInt() == 0) {
                val heartRate = data[1].toInt() and 0xFF
                listener.onNotify(heartRate)
            }
        }
    }

    /**
     * Notify for connection results

     * @param result True, if connected. False if disconnected
     */
    private fun notifyConnectionResult(result: Boolean) {
        connectionSubject.onNext(result)
        connectionSubject.onComplete()

        // create new connection subject
        connectionSubject = PublishSubject.create<Boolean>()
    }

    override fun onConnectionEstablished() {
        notifyConnectionResult(true)
    }

    override fun onDisconnected() {
        notifyConnectionResult(false)

        //start locking service and show toast Band disconnected
        Intent(context, LockingService::class.java).also {
            it.action = LockingServiceActions.START.name
            startForegroundService(context, it)
        }

        Timber.d("MiBand disconnected")
    }

    override fun onResult(data: BluetoothGattCharacteristic) {
        val serviceId = data.service.uuid
        val characteristicId = data.uuid
        if (serviceId == Profile.UUID_SERVICE_MILI) {

            // Battery info
            if (characteristicId == Profile.UUID_CHAR_BATTERY) {
                Timber.d("getBatteryInfo result ${Arrays.toString(data.value)}")
                if (data.value.size == 10) {
                    val info = BatteryInfo.fromByteData(data.value)

                    batteryInfoSubject.onNext(info)
                    batteryInfoSubject.onComplete()
                } else {
                    batteryInfoSubject.onError(Exception("Wrong data format for battery info"))
                }
                batteryInfoSubject = PublishSubject.create()
            }

            // sensor notify
            if (characteristicId == Profile.UUID_CHAR_CONTROL_POINT) {
                val changedValue = data.value
                if (Arrays.equals(changedValue, Protocol.ENABLE_SENSOR_DATA_NOTIFY)) {
                    sensorNotificationSubject.onNext(true)
                } else {
                    sensorNotificationSubject.onNext(false)
                }
                sensorNotificationSubject.onComplete()
                sensorNotificationSubject = PublishSubject.create()
            }

            // realtime notify
            if (characteristicId == Profile.UUID_CHAR_CONTROL_POINT) {
                val changedValue = data.value
                if (Arrays.equals(changedValue, Protocol.ENABLE_REALTIME_STEPS_NOTIFY)) {
                    realtimeNotificationSubject.onNext(true)
                } else {
                    realtimeNotificationSubject.onNext(false)
                }
                realtimeNotificationSubject.onComplete()
                realtimeNotificationSubject = PublishSubject.create()
            }

            // led color
            if (characteristicId == Profile.UUID_CHAR_CONTROL_POINT) {
                val changedValue = data.value
                var ledColor = LedColor.BLUE
                if (Arrays.equals(changedValue, Protocol.SET_COLOR_RED)) {
                    ledColor = LedColor.RED
                } else if (Arrays.equals(changedValue, Protocol.SET_COLOR_BLUE)) {
                    ledColor = LedColor.BLUE
                } else if (Arrays.equals(changedValue, Protocol.SET_COLOR_GREEN)) {
                    ledColor = LedColor.GREEN
                } else if (Arrays.equals(changedValue, Protocol.SET_COLOR_ORANGE)) {
                    ledColor = LedColor.ORANGE
                }
                ledColorSubject.onNext(ledColor)
                ledColorSubject.onComplete()
                ledColorSubject = PublishSubject.create()
            }

            // user info
            if (characteristicId == Profile.UUID_CHAR_USER_INFO) {
                userInfoSubject.onComplete()

                userInfoSubject = PublishSubject.create()
            }
        }

        if (serviceId == Profile.UUID_SERVICE_MIBAND2) {
            // pair
            if (characteristicId == Profile.UUID_CHAR_PAIR) {
                Timber.d("pair requested $pairRequested")
                if (pairRequested) {
                    pairRequested = false
                } else {
                    pairInitSubject.onComplete()
                }
                pairInitSubject = PublishSubject.create()
            }
        }

        // vibration service
        if (serviceId == Profile.UUID_SERVICE_VIBRATION) {
            if (characteristicId == Profile.UUID_CHAR_VIBRATION) {
                val changedValue = data.value
                if (Arrays.equals(changedValue, Protocol.STOP_VIBRATION)) {
                    stopVibrationSubject.onComplete()
                    stopVibrationSubject = PublishSubject.create()
                } else {
                    startVibrationSubject.onComplete()
                    startVibrationSubject = PublishSubject.create()
                }
            }
        }

        // heart rate (fix this crap)
        if (serviceId == Profile.UUID_SERVICE_HEARTRATE) {
            if (characteristicId == Profile.UUID_CHAR_HEARTRATE) {
                val changedValue = data.value
                if (Arrays.equals(changedValue, Protocol.PING_HR_MONITOR)) {
                    heartRateSubject.onComplete()
                    heartRateSubject = PublishSubject.create()
                }
            }
        }


        //Device Information values
        if(serviceId == Profile.UUID_SERVICE_DEVICE_INFORMATION){
            if (characteristicId == Profile.UUID_CHAR_SERIAL_NUMBER){
                val changedValue = data.getStringValue(0).trim()
                serialNumberSubject.onNext(changedValue)
                serialNumberSubject.onComplete()
                serialNumberSubject = PublishSubject.create()
            }
            if (characteristicId == Profile.UUID_CHAR_HARDWARE_REVISION){
                val changedValue = data.getStringValue(0).trim()
                hardwareRevisionSubject.onNext(changedValue)
                hardwareRevisionSubject.onComplete()
                hardwareRevisionSubject = PublishSubject.create()
            }
            if (characteristicId == Profile.UUID_CHAR_SOFTWARE_REVISION){
                val changedValue = data.getStringValue(0).trim()
                softwareRevisionSubject.onNext(changedValue)
                softwareRevisionSubject.onComplete()
                softwareRevisionSubject = PublishSubject.create()
            }
        }
    }

    override fun onResultRssi(rssi: Int) {
        rssiSubject.onNext(rssi)
        rssiSubject.onComplete()

        rssiSubject = PublishSubject.create()
    }

    override fun onFail(serviceUUID: UUID, characteristicId: UUID, msg: String) {
        if (serviceUUID == Profile.UUID_SERVICE_MILI) {

            // Battery info
            if (characteristicId == Profile.UUID_CHAR_BATTERY) {
                Timber.d("getBatteryInfo failed: $msg")
                batteryInfoSubject.onError(Exception("Wrong data format for battery info"))
                batteryInfoSubject = PublishSubject.create()
            }

            // sensor notify
            if (characteristicId == Profile.UUID_CHAR_CONTROL_POINT) {
                Timber.d("Sensor notify failed $msg")
                sensorNotificationSubject.onError(Exception("Sensor notify failed"))
                sensorNotificationSubject = PublishSubject.create()
            }

            // realtime notify
            if (characteristicId == Profile.UUID_CHAR_CONTROL_POINT) {
                Timber.d("Realtime notify failed $msg")
                realtimeNotificationSubject.onError(Exception("Realtime notify failed"))
                realtimeNotificationSubject = PublishSubject.create()
            }

            // led color
            if (characteristicId == Profile.UUID_CHAR_CONTROL_POINT) {
                Timber.d("Led color failed")
                ledColorSubject.onError(Exception("Changing LED color failed"))
                ledColorSubject = PublishSubject.create()
            }

            // user info
            if (characteristicId == Profile.UUID_CHAR_USER_INFO) {
                Timber.d("User info failed")
                userInfoSubject.onError(Exception("Setting User info failed"))
                userInfoSubject = PublishSubject.create()
            }
        }

        if (serviceUUID == Profile.UUID_SERVICE_MIBAND2) {
            // Pair
            if (characteristicId == Profile.UUID_CHAR_PAIR) {
                Timber.d(msg)
                pairInitSubject.onError(Exception("Pairing failed"))
                pairInitSubject = PublishSubject.create()
            }
        }

        // vibration service
        if (serviceUUID == Profile.UUID_SERVICE_VIBRATION) {
            if (characteristicId == Profile.UUID_CHAR_VIBRATION) {
                Timber.d("Enable/disable vibration failed")
                stopVibrationSubject.onError(Exception("Enable/disable vibration failed"))
                stopVibrationSubject = PublishSubject.create()
            }
        }

        // heart rate
        if (serviceUUID == Profile.UUID_SERVICE_HEARTRATE) {
            if (characteristicId == Profile.UUID_CHAR_HEARTRATE) {
                Timber.d("Reading heartrate failed")
                heartRateSubject.onError(Exception("Reading heartrate failed"))
                heartRateSubject = PublishSubject.create()
            }
        }
    }

    override fun onFail(errorCode: Int, msg: String) {
        Timber.d(String.format("onFail: errorCode %d, message %s", errorCode, msg))
        when (errorCode) {
            ERROR_CONNECTION_FAILED -> {
                connectionSubject.onError(Exception("Establishing connection failed"))
                connectionSubject = PublishSubject.create()
            }
            ERROR_READ_RSSI_FAILED -> {
                rssiSubject.onError(Exception("Reading RSSI failed"))
                rssiSubject = PublishSubject.create()
            }
        }
    }
}
