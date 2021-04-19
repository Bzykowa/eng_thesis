package com.example.lockband.miband3

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
import com.example.lockband.miband3.listeners.HeartRateNotifyListener
import com.example.lockband.miband3.listeners.RealtimeStepsNotifyListener
import com.example.lockband.miband3.model.*
import com.example.lockband.services.LockingService
import com.example.lockband.utils.*
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
    fun requestRandomNumber() {
        Timber.d("Request random number from MiBand")
        bluetoothIo.writeCharacteristic(
            Profile.UUID_SERVICE_MIBAND2,
            Profile.UUID_CHAR_PAIR,
            Protocol.REQ_RAND_NUMBER
        )
    }

    /**
     * Send encrypted number to band
     */
    fun sendEncryptedNumber(num: ByteArray) {

        Timber.d("Send encrypted number to MiBand")

        val encryptedNum = encryptAES(num, KEY)
        val message = Protocol.SEND_ENC_NUMBER + encryptedNum


        bluetoothIo.writeCharacteristic(
            Profile.UUID_SERVICE_MIBAND2,
            Profile.UUID_CHAR_PAIR,
            message
        )

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

    fun requestMtu(mtu: Int): Observable<Int> = Observable.create<Int> {
        mtuSubject.subscribe(ObserverWrapper(it))
        bluetoothIo.requestMtu(mtu)
    }

    /**
     * Initial set up methods   ********************************************************************
     */


    /**
     * Set english language in band
     */
    fun setEnglishLanguage() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.SET_ENGLISH_LANGUAGE
    )

    /**
     * Disable screen unlock in band
     */
    fun disableScreenUnlock() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DISABLE_BAND_SCREEN_UNLOCK
    )

    /**
     * Disable night mode in band
     */
    fun disableNightMode() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_NIGHT_MODE_OFF
    )

    /**
     * Set date format to dd/MM/yyyy
     */
    fun setDateFormat() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.DATEFORMAT_DATE_DD_MM_YYYY
    )


    /**
     * Send current time to band
     */
    fun setCurrentTime(): Observable<Void> = Observable.create<Void> {
        timeSubject.subscribe(ObserverWrapper(it))
        bluetoothIo.writeCharacteristic(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_DATA_TIME,
            getTimeBytes(Conversions.createCalendar(), TimeUnit.SECONDS)!!
        )
    }


    /**
     * Sends date display configuration to band
     */
    fun setDateDisplay() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.DATEFORMAT_DATE_TIME
    )


    /**
     * Sends time format config to band
     */
    fun setTimeFormat() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.DATEFORMAT_TIME_24_HOURS
    )

    /**
     * Sets user info

     * @param userInfo User info
     */
    fun setUserInfo(userInfo: UserInfo): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            userInfoSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.writeCharacteristic(
                Profile.UUID_SERVICE_MILI,
                Profile.UUID_CHAR_CONTROL_POINT,
                userInfo.getBytes()
            )
        }
    }

    /**
     * Sends unit configuration to band
     */
    fun setMetricUnits() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DISTANCE_UNIT_METRIC
    )


    /**
     * Set wear location to left hand
     */
    fun setWearLocation() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_USER_INFO,
        Protocol.WEAR_LOCATION_LEFT_WRIST
    )

    /**
     * Sets up fitness goal (10000 steps)
     */
    fun setFitnessGoal(): Observable<Void> = Observable.create<Void> { subscriber ->
        Timber.d("Setting up fitness goal")
        userInfoSubject.subscribe(ObserverWrapper(subscriber))
        bluetoothIo.writeCharacteristic(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_USER_INFO,
            Protocol.COMMAND_SET_FITNESS_GOAL_START + Conversions.fromUint16(stepGoal) + Protocol.COMMAND_SET_FITNESS_GOAL_END
        )
    }

    /**
     * Set up default items displayed in band menu
     */
    fun setDisplayItems() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.DISPLAY_ITEMS_DEFAULT
    )

    /**
     * Disable Do Not Distrurb mode
     */
    fun disableDND() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DO_NOT_DISTURB_OFF
    )

    /**
     * Disable rotate wrist gesture action
     */
    fun disableRotateWristToSwitchInfo() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DISABLE_ROTATE_WRIST_TO_SWITCH_INFO
    )

    /**
     * Disable lift wrist gesture action
     */
    fun disableLiftWristToActivateDisplay() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DISABLE_DISPLAY_ON_LIFT_WRIST
    )

    /**
     * Enable displaying caller
     */
    fun enableDisplayCaller() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_ENABLE_DISPLAY_CALLER
    )

    /**
     * Disable step goal notifications
     */
    fun disableGoalNotification() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DISABLE_GOAL_NOTIFICATION
    )

    /**
     * Disable Inactivity Warnings
     */
    fun disableInactivityWarnings() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_DISABLE_INACTIVITY_WARNINGS
    )

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
     * Enable onDisconnect Notification
     */
    fun enableDisconnectNotification() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_ENABLE_DISCONNECT_NOTIFCATION
    )

    /**
     * Disable BT connected advertisement
     */
    fun enableBTConnectedAdvertisement() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_ENABLE_BT_CONNECTED_ADVERTISEMENT
    )

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

    fun requestAlarms() = bluetoothIo.writeCharacteristic(
        Profile.UUID_SERVICE_MILI,
        Profile.UUID_CHAR_CONTROL_POINT,
        Protocol.COMMAND_REQUEST_ALARMS
    )

    /**
     * Listeners    ********************************************************************************
     */

    /**
     * Set listener for UserInfo characteristic
     */
    fun setUserInfoListener(listener: (ByteArray) -> Unit) {
        Timber.d("Setting up listener on User Info characteristic")
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_USER_INFO,
            listener
        )
    }

    /**
     * Remove listener for UserInfo characteristic
     */
    fun removeUserInfoListener() {
        Timber.d("Disabling listener on User Info characteristic")
        bluetoothIo.removeNotifyListener(
            Profile.UUID_SERVICE_MILI,
            Profile.UUID_CHAR_USER_INFO
        )
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

    /**
     * Sets realtime steps notification listener

     * @param listener Notification listener
     */
    fun setRealtimeStepsNotifyListener(listener: RealtimeStepsNotifyListener) {
        bluetoothIo.setNotifyListener(
            Profile.UUID_SERVICE_MIBAND2,
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
     * Sets heart rate scanner listener
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
     * Device information getters   ****************************************************************
     */

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
     * Read band serial number
     */
    fun readSerialNumber(): Observable<String> {
        return Observable.create<String> { subscriber ->
            serialNumberSubject.subscribe(ObserverWrapper(subscriber))
            bluetoothIo.readCharacteristic(
                Profile.UUID_SERVICE_DEVICE_INFORMATION,
                Profile.UUID_CHAR_SERIAL_NUMBER
            )
        }
    }

    /**
     * Read band hardware version
     */
    fun readHardwareRevision(): Observable<String> {
        return Observable.create<String> {
            hardwareRevisionSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.readCharacteristic(
                Profile.UUID_SERVICE_DEVICE_INFORMATION,
                Profile.UUID_CHAR_HARDWARE_REVISION
            )
        }
    }

    /**
     * Read band software version
     */
    fun readSoftwareRevision(): Observable<String> {
        return Observable.create<String> {
            softwareRevisionSubject.subscribe(ObserverWrapper(it))
            bluetoothIo.readCharacteristic(
                Profile.UUID_SERVICE_DEVICE_INFORMATION,
                Profile.UUID_CHAR_SOFTWARE_REVISION
            )
        }
    }

    /**
     * Vibration methods   *************************************************************************
     */

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
     * BT methods   ********************************************************************************
     */

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


        when (serviceId) {
            //Device Information values
            Profile.UUID_SERVICE_DEVICE_INFORMATION -> {
                when (characteristicId) {
                    Profile.UUID_CHAR_SERIAL_NUMBER -> {
                        val changedValue = data.getStringValue(0).trim()
                        serialNumberSubject.onNext(changedValue)
                        serialNumberSubject.onComplete()
                        serialNumberSubject = PublishSubject.create()
                    }
                    Profile.UUID_CHAR_HARDWARE_REVISION -> {
                        val changedValue = data.getStringValue(0).trim()
                        hardwareRevisionSubject.onNext(changedValue)
                        hardwareRevisionSubject.onComplete()
                        hardwareRevisionSubject = PublishSubject.create()
                    }
                    Profile.UUID_CHAR_SOFTWARE_REVISION -> {
                        val changedValue = data.getStringValue(0).trim()
                        softwareRevisionSubject.onNext(changedValue)
                        softwareRevisionSubject.onComplete()
                        softwareRevisionSubject = PublishSubject.create()
                    }
                }
            }
            // heart rate (fix this crap)
            Profile.UUID_SERVICE_HEARTRATE -> {
                if (characteristicId == Profile.UUID_CHAR_HEARTRATE) {
                    val changedValue = data.value
                    if (Arrays.equals(changedValue, Protocol.PING_HR_MONITOR)) {
                        heartRateSubject.onComplete()
                        heartRateSubject = PublishSubject.create()
                    }
                }
            }
            // vibration service
            Profile.UUID_SERVICE_VIBRATION -> {
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
            Profile.UUID_SERVICE_MIBAND2 -> {
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
            Profile.UUID_SERVICE_MILI -> {
                // Battery info
                when (characteristicId) {
                    Profile.UUID_CHAR_BATTERY -> {
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

                    // user info
                    Profile.UUID_CHAR_USER_INFO -> {
                        userInfoSubject.onComplete()
                        userInfoSubject = PublishSubject.create()
                    }
                }
            }
        }
    }

    override fun onResultRssi(rssi: Int) {
        rssiSubject.onNext(rssi)
        rssiSubject.onComplete()

        rssiSubject = PublishSubject.create()
    }

    override fun onFail(serviceUUID: UUID, characteristicId: UUID, msg: String, data: ByteArray) {
        when (serviceUUID) {
            Profile.UUID_SERVICE_MILI -> {

                // Battery info
                when (characteristicId) {
                    Profile.UUID_CHAR_BATTERY -> {
                        Timber.d("getBatteryInfo failed: $msg")
                        batteryInfoSubject.onError(Exception("Wrong data format for battery info"))
                        batteryInfoSubject = PublishSubject.create()
                    }
                    //TODO("Possible multiple calls. Write fun if needed")
                    // user info
                    Profile.UUID_CHAR_USER_INFO -> {
                        Timber.d("User info failed")
                        userInfoSubject.onError(Exception("Setting User info failed"))
                        userInfoSubject = PublishSubject.create()
                    }
                }
            }

            Profile.UUID_SERVICE_MIBAND2 -> {
                when (characteristicId) {
                    // Pair
                    Profile.UUID_CHAR_PAIR -> {
                        Timber.d(msg)
                        pairInitSubject.onError(Exception("Pairing failed"))
                        pairInitSubject = PublishSubject.create()
                    }
                }
            }
            // vibration service
            Profile.UUID_SERVICE_VIBRATION -> {
                if (characteristicId == Profile.UUID_CHAR_VIBRATION) {
                    Timber.d("Enable/disable vibration failed")
                    stopVibrationSubject.onError(Exception("Enable/disable vibration failed"))
                    stopVibrationSubject = PublishSubject.create()
                }
            }
            // heart rate
            Profile.UUID_SERVICE_HEARTRATE -> {
                if (characteristicId == Profile.UUID_CHAR_HEARTRATE) {
                    Timber.d("Reading heartrate failed")
                    heartRateSubject.onError(Exception("Reading heartrate failed"))
                    heartRateSubject = PublishSubject.create()
                }
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
