package com.example.lockband

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lockband.adapters.DeviceListAdapter
import com.example.lockband.data.DataGatheringServiceActions
import com.example.lockband.databinding.ActivityPairingBinding
import com.example.lockband.services.DataGatheringService
import com.example.lockband.utils.setMiBandAddress
import com.khmelenko.lab.miband.MiBand
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityPairingBinding>(this, R.layout.activity_pairing)

        miBand = MiBand(this)
        adapter = DeviceListAdapter(this, R.layout.device_item, R.id.deviceName, ArrayList())

        scanButton.setOnClickListener {
            val disposable = miBand.startScan()
                .subscribe(handleScanResult(), handleScanError())
            disposables.add(disposable)
        }

        //set up adapter plus pairing listener
        deviceList.adapter = adapter
        deviceList.setOnItemClickListener { parent, view, position, id ->
            val item =
                view.rootView.findViewById<TextView>(R.id.deviceName).text.toString()           //.deviceName.text.toString()

            if (devices.containsKey(item)) {

                val disposable = miBand.stopScan().subscribe(handleScanResult(), handleScanError())
                disposables.add(disposable)

                setMiBandAddress(this, devices[item]!!.address)
                devices[item]!!.createBond()

                Intent(this@PairingActivity, DataGatheringService::class.java).also {
                    it.putExtra("device", devices[item])
                    it.action = DataGatheringServiceActions.PAIR.name
                    startForegroundService(it)
                }

                Intent(this@PairingActivity, MainActivity::class.java).also {
                    startActivity(it)
                }

            }
        }

    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun handleScanResult(): Consumer<ScanResult> {
        return Consumer { result ->
            val device = result.device

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
            Timber.e(throwable)
        }
    }
}
