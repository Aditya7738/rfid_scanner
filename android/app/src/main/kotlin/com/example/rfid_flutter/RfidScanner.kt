import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import com.darryncampbell.datawedgeflutter.DWInterface
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.logging.StreamHandler
import org.json.JSONObject

// object DWInterface {
//     const val DATAWEDGE_RETURN_ACTION = "com.symbol.datawedge.api.RESULT_ACTION"
//     const val DATAWEDGE_RETURN_CATEGORY = "android.intent.category.DEFAULT"
// }

class RFIDScanner : FlutterActivity() {
    val dwInterface = DWInterface()

    // private val PROFILE_INTENT_ACTION = "com.darryncampbell.datawedgeflutter.SCAN"

   // private val PROFILE_INTENT_ACTION = "com.example.rfid_flutter.SCAN"

    // private val SCAN_CHANNEL = "com.darryncampbell.datawedgeflutter/scan"

    // private val COMMAND_CHANNEL = "com.darryncampbell.datawedgeflutter/command"

    // private val SCAN_CHANNEL = "com.example.rfid_flutter/scan"
    // private val COMMAND_CHANNEL = "com.example.rfid_flutter/command"

    // override fun configureFlutterEngine(flutterEngine: FlutterEngine) {

    //     GeneratedPluginRegistrant.registerWith(flutterEngine)

    //     EventChannel(flutterEngine.dartExecutor, SCAN_CHANNEL)
    //             .setStreamHandler(
    //                     object : EventChannel.StreamHandler {
    //                         private var dataWedgeBroadcastReceiver: BroadcastReceiver? = null
    //                         override fun onListen(arguments: Any?, events: EventSink?) {

    //                             dataWedgeBroadcastReceiver =
    //                                     createDataWedgeBroadcastReceiver(events)

    //                             val intentFilter = IntentFilter()
    //                             intentFilter.addAction(PROFILE_INTENT_ACTION)
    //                             intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
    //                             intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)

    //                             registerReceiver(dataWedgeBroadcastReceiver, intentFilter)
    //                         }

    //                         override fun onCancel(arguments: Any?) {
    //                             unregisterReceiver(dataWedgeBroadcastReceiver)
    //                             dataWedgeBroadcastReceiver = null
    //                         }
    //                     }
    //             )

    //     MethodChannel(flutterEngine.dartExecutor, COMMAND_CHANNEL).setMethodCallHandler {
    //             call,
    //             result ->
    //         if (call.method == "sendDataWedgeCommandStringParameter") {

    //             Log.d("RFIDScanner", "sendDataWedgeCommandStringParameter")

    //             val arguments = JSONObject(call.arguments.toString())
    //             val command: String = arguments.get("command") as String
    //             val parameter: String = arguments.get("parameter") as String
    //             if (dwInterface.sendCommandString(applicationContext, command, parameter)) {
    //                 result.success("Command sent successfully")
    //             } else {
    //                 result.error("UNSUCCESS", "Command not sent", null)
    //             }
    //             //  result.success(0);  //  DataWedge does not return responses
    //         } else if (call.method == "createProfile") {

    //             Log.d("RFIDScanner", "createProfile")
    //             if (createProfile(call.arguments.toString())) {
    //                 result.success("Profile created successfully")
    //             } else {
    //                 result.error("UNSUCCESS", "Profile is not created", null)
    //             }
    //         } else {
    //             result.notImplemented()
    //         }
    //     }
    // }

    // private fun createDataWedgeBroadcastReceiver(events: EventSink?): BroadcastReceiver? {
    //     return object : BroadcastReceiver() {

    //         override fun onReceive(context: Context, intent: Intent) {

    //             if (intent.action.equals(PROFILE_INTENT_ACTION)) {
    //                 //  A barcode has been scanned
    //                 var scanData =
    //                         intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
    //                 var symbology =
    //                         intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
    //                 var date = Calendar.getInstance().getTime()
    //                 var df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    //                 var dateTimeString = df.format(date)
    //                 var currentScan = Scan(scanData, symbology, dateTimeString)
    //                 events?.success(currentScan.toJson())
    //             }
    //         }
    //     }
    // }

    //  Could handle return values from DW here such as RETURN_GET_ACTIVE_PROFILE
    //  or RETURN_ENUMERATE_SCANNERS

    // private val PROFILE_INTENT_BROADCAST = "2"

    // private fun createProfile(profileName: String): Boolean {
    //     //  Create and configure the DataWedge profile associated with this application
    //     //  For readability's sake, I have not defined each of the keys in the DWInterface file
    //     dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_CREATE_PROFILE, profileName)
    //     val profileConfig = Bundle()
    //     profileConfig.putString("PROFILE_NAME", profileName)
    //     profileConfig.putString("PROFILE_ENABLED", "true") //  These are all strings
    //     profileConfig.putString("CONFIG_MODE", "UPDATE")
    //     val barcodeConfig = Bundle()
    //     barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
    //     barcodeConfig.putString(
    //             "RESET_CONFIG",
    //             "true"
    //     ) //  This is the default but never hurts to specify
    //     val barcodeProps = Bundle()
    //     barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
    //     profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
    //     val appConfig = Bundle()
    //     appConfig.putString("PACKAGE_NAME", packageName) //  Associate the profile with this app
    //     appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
    //     profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
    //     dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
    //     //  You can only configure one plugin at a time in some versions of DW, now do the intent
    //     // output
    //     profileConfig.remove("PLUGIN_CONFIG")
    //     val intentConfig = Bundle()
    //     intentConfig.putString("PLUGIN_NAME", "INTENT")
    //     intentConfig.putString("RESET_CONFIG", "true")

    //     val intentProps = Bundle()
    //     intentProps.putString("intent_output_enabled", "true")
    //     intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
    //     intentProps.putString("intent_delivery", PROFILE_INTENT_BROADCAST) //  "2"

    //     intentConfig.putBundle("PARAM_LIST", intentProps)

    //     profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)

    //     return dwInterface.sendCommandBundle(
    //             this,
    //             DWInterface.DATAWEDGE_SEND_SET_CONFIG,
    //             profileConfig
    //     )
    // }
}
