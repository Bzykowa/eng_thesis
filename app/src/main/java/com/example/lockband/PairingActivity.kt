package com.example.lockband

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.adapters.DeviceListAdapter
import com.example.lockband.data.MiBandServiceActions
import com.example.lockband.databinding.ActivityPairingBinding
import com.example.lockband.services.MiBandService
import com.example.lockband.utils.setMiBandAddress
import com.example.lockband.miband3.MiBand
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_pairing.*
import kotlinx.android.synthetic.main.device_item.view.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class PairingActivity : AppCompatActivity() {

    private lateinit var miBand: MiBand

    private val devices = HashMap<String, BluetoothDevice>()
    private lateinit var adapter: ArrayAdapter<String>

    private val disposables = CompositeDisposable()

    private lateinit var item : String

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            with(intent) {
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)

                    if(bondState == BluetoothDevice.BOND_BONDED){
                        Intent(this@PairingActivity, MiBandService::class.java).also {
                            it.putExtra("device", devices[item])
                            it.action = MiBandServiceActions.PAIR.name
                            startForegroundService(it)
                        }

                        Intent(this@PairingActivity, MainActivity::class.java).also {
                            startActivity(it)
                        }
                    }

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityPairingBinding>(this, R.layout.activity_pairing)

        miBand = MiBand(this)
        adapter = DeviceListAdapter(this, R.layout.device_item, R.id.deviceName, ArrayList())

        scanButton.setOnClickListener {
            scanButton.text = getString(R.string.pairing_scan_stop)
            val disposable = miBand.startScan()
                .subscribe(handleScanResult(), handleScanError())
            disposables.add(disposable)
        }

        //set up adapter plus pairing listener
        deviceList.adapter = adapter
        deviceList.setOnItemClickListener { _, view, _, _ ->
            item = view.rootView.findViewById<TextView>(R.id.deviceName).text.toString()           //.deviceName.text.toString()
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            registerReceiver(broadcastReceiver, intentFilter)

            if (devices.containsKey(item)) {

                val disposable = miBand.stopScan().subscribe(handleScanResult(), handleScanError())
                disposables.add(disposable)

                setMiBandAddress(this, devices[item]!!.address)
                devices[item]!!.createBond()

            }
        }

    }

    override fun onDestroy() {
        disposables.clear()
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private fun handleScanResult(): Consumer<ScanResult> {
        return Consumer { result ->
            val device = result.device
            scanButton.text = getString(R.string.scan_for_devices)

            Timber.d("Scan results: name: ${device.name} uuid: ${Arrays.toString(device.uuids)}, add: ${device.address}, type: ${device.type} bondState: ${device.bondState}, rssi: ${result.rssi}")

            val item = "${device.name} (${device.address})"

            if (!devices.containsKey(item) && device.name == "Mi Band 3") {
                devices[item] = device
                adapter.add(item)
            }
        }
    }

    private fun handleScanError(): Consumer<Throwable> {
        return Consumer { throwable ->
            scanButton.text = getString(R.string.scan_for_devices)
            Toast.makeText(this, "Scan failed. Try again.",Toast.LENGTH_SHORT).show()
            Timber.e(throwable)
        }
    }
}
