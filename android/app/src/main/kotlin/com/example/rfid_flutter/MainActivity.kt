package com.example.rfid_flutter

import Scan
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.darryncampbell.datawedgeflutter.DWInterface
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID
import java.util.logging.StreamHandler
import org.json.JSONObject

private const val REQUEST_ENABLE_BT: Int = 1

class MainActivity : FlutterActivity() {

    val dwInterface = DWInterface()

    private val CHANNEL = "com.example.rfid_flutter"

    private val SCAN_CHANNEL = "com.example.rfid_flutter/scan"
    //  private val COMMAND_CHANNEL = "com.example.rfid_flutter/command"

    private val PROFILE_INTENT_ACTION = "com.example.rfid_flutter.SCAN"

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

        //  GeneratedPluginRegistrant.registerWith(flutterEngine)

        // GeneratedPluginRegistrant.registerWith(flutterEngine)

        EventChannel(flutterEngine.dartExecutor, SCAN_CHANNEL)
                .setStreamHandler(
                        object : EventChannel.StreamHandler {
                            private var dataWedgeBroadcastReceiver: BroadcastReceiver? = null
                            override fun onListen(arguments: Any?, events: EventSink?) {

                                dataWedgeBroadcastReceiver =
                                        createDataWedgeBroadcastReceiver(events)

                                val intentFilter = IntentFilter()
                                intentFilter.addAction(PROFILE_INTENT_ACTION)
                                intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
                                intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)
                                intentFilter.addAction(PROFILE_INTENT_ACTION)
                                intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
                                intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)
                                val intent = Intent()
                                intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
                                val listenToBroadcastsFromOtherApps = true
                                val receiverFlags =
                                        if (listenToBroadcastsFromOtherApps) {  
                                            ContextCompat.RECEIVER_EXPORTED
                                        } else {
                                            ContextCompat.RECEIVER_NOT_EXPORTED
                                        }
                               // registerReceiver(dataWedgeBroadcastReceiver, intentFilter)
                               ContextCompat.registerReceiver(context, dataWedgeBroadcastReceiver, intentFilter, null, null, receiverFlags)
                            }

                            override fun onCancel(arguments: Any?) {
                                unregisterReceiver(dataWedgeBroadcastReceiver)
                                dataWedgeBroadcastReceiver = null
                            }
                        }
                )

        MethodChannel(flutterEngine.dartExecutor, CHANNEL).setMethodCallHandler {
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

            if (
                call.method == "sendCommandString"    
            //call.method == "sendDataWedgeCommandStringParameter"
            ) {

                Log.d("RFIDScanner", "sendDataWedgeCommandStringParameter")

                val arguments = JSONObject(call.arguments.toString())
                // val command: String = arguments.get("command") as String
                // val parameter: String = arguments.get("parameter") as String
                if (
                    //dwInterface.sendCommandString(applicationContext, command, parameter
                    dwInterface.sendCommandString(applicationContext, arguments.get("command") as String, arguments.get("parameter") as String
                    )) {
                    result.success("Command sent successfully")
                } else {
                    result.error("UNSUCCESS", "Command not sent", null)
                }
                //  result.success(0);  //  DataWedge does not return responses
            } else {
                result.notImplemented()
            }

            if (call.method == "createProfile") {

                Log.d("RFIDScanner", "createProfile")
                if (createProfile(
                    //call.arguments.toString()
                    )) {
                    result.success("Profile created successfully")
                } else {
                    result.error("UNSUCCESS", "Profile is not created", null)
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

            Log.d("MAINACTIVITY", "Device Name: $deviceName")
            Log.d("MAINACTIVITY", "Device MAC Address: $deviceHardwareAddress")
        }

        // return pairedDevices
        return listOfConnectedRFIDScanners.isNotEmpty()
    }

    private val PROFILE_INTENT_BROADCAST = "2"

     fun createProfile(): Boolean {
        //  Create and configure the DataWedge profile associated with this application
        //  For readability's sake, I have not defined each of the keys in the DWInterface file
        dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_CREATE_PROFILE, "profileName")
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", "profileName")
        profileConfig.putString("PROFILE_ENABLED", "true") //  These are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString(
                "RESET_CONFIG",
                "true"
        ) //  This is the default but never hurts to specify
        val barcodeProps = Bundle()
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", packageName) //  Associate the profile with this app
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
        //  You can only configure one plugin at a time in some versions of DW, now do the intent
        // output
        profileConfig.remove("PLUGIN_CONFIG")
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")

        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
        intentProps.putString("intent_delivery", PROFILE_INTENT_BROADCAST) //  "2"

        intentConfig.putBundle("PARAM_LIST", intentProps)

        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)

        return dwInterface.sendCommandBundle(
                this,
                DWInterface.DATAWEDGE_SEND_SET_CONFIG,
                profileConfig
        )
    }

     fun createDataWedgeBroadcastReceiver(events: EventSink?): BroadcastReceiver? {
        return object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                if (intent.action.equals(PROFILE_INTENT_ACTION)) {
                    //  A barcode has been scanned
                    var scanData =
                            intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
                    var symbology =
                            intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
                    var date = Calendar.getInstance().getTime()
                    var df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    var dateTimeString = df.format(date)
                    var currentScan = Scan(scanData, symbology, dateTimeString)
                    events?.success(currentScan.toJson())
                }
            }
        }
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
}
