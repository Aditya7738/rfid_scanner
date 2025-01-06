package com.example.rfid_flutter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.IOException
import java.util.UUID

private const val REQUEST_ENABLE_BT: Int = 1

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.example.rfid_flutter"

    // val bluetoothManager: BluetoothManager? = getSystemService(BluetoothManager::class.java)
    // val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onStart() {
        super.onStart()
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            bluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {

        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call,
                result ->
            // This method is invoked on the main thread.
            // TODO

            if (call.method == "isBluetoothSupported") {

                if (isBluetoothSupported()) {
                    result.success("Bluetooth is supported")
                    // result.success(p0)
                } else {
                    result.error("UNAVAILABLE", "Bluetooth is not supported", null)
                }
            } else {
                result.notImplemented()
            }

            if (call.method == "getPairedDevices") {

                print("Getting paired devices")
                Log.d("BLUETOOTH", "getPairedBTDevices called")

                if (getPairedDevices()) {
                    result.success("RFID Scanner is connected")
                    // result.success(p0)
                } else {
                    result.error("Not found", "RFID Scanner not found", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    fun enableBluetooth() {
        if (bluetoothAdapter!!.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    fun isBluetoothSupported(): Boolean {

        if (bluetoothAdapter == null) {
            Log.d("BLUETOOTH", "Bluetooth is not supported")
            return false
        } else {
            Log.d("BLUETOOTH", "Bluetooth is supported")
            enableBluetooth()
            return true
        }
    }

    fun getPairedDevices(): Boolean {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        // var rfidDevices: Set<BluetoothDevice> = mutableSetOf()

        var listOfConnectedRFIDScanners: MutableList<String> = mutableListOf()

        pairedDevices?.forEach { device ->
            val deviceName: String = device.name
            val deviceHardwareAddress = device.address // MAC address

            deviceName.let {
                if (deviceName.contains("RFD")) {
                    listOfConnectedRFIDScanners.add(deviceName)

                    val connectThread = ConnectThread(device)
                    connectThread.start()
                }
            }

            Log.d("MAINACTIVITY","Device Name: $deviceName")
            Log.d("MAINACTIVITY","Device MAC Address: $deviceHardwareAddress")
        }

        // return pairedDevices
        return listOfConnectedRFIDScanners.isNotEmpty()
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by
                lazy(LazyThreadSafetyMode.NONE) {
                    // get a BluetoothSocket
                    device.createRfcommSocketToServiceRecord(MY_UUID)
                }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            // comment below line if you don't want to cancel discovery
            // bluetoothAdapter?.cancelDiscovery()

            // mmSocket?.let { socket ->
            //     // Connect to the remote device through the socket. This call blocks
            //     // until it succeeds or throws an exception.
            //     socket.connect()

            //     // The connection attempt succeeded. Perform work associated with
            //     // the connection in a separate thread.

            //   //  manageMyConnectedSocket(socket)
            // }

            try {
                // Cancel discovery if it's ongoing
                bluetoothAdapter?.cancelDiscovery()

                mmSocket?.let { socket ->
                    // Connect to the remote device
                    socket.connect()

                    // Handle successful connection
                    // manageMyConnectedSocket(socket)
                }
            } catch (e: IOException) {
                // Handle connection errors
                Log.e("BluetoothConnection", "Connection failed: ${e.message}")
                // Close the socket if it was opened
                mmSocket?.close()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("BLUETOOTH ERROR", "Could not close the client socket ${e.toString()}", e)
            }
        }
    }

    private val PROFILE_INTENT_ACTION = "com.darryncampbell.datawedgeflutter.SCAN"
    private val SCAN_CHANNEL = "com.darryncampbell.datawedgeflutter/scan"
}
