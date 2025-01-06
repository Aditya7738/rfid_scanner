// ignore_for_file: prefer_interpolation_to_compose_strings

import 'dart:convert';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:rfid_flutter/controller/select_device_controller.dart';

class SelectBLEDevice extends StatefulWidget {
  const SelectBLEDevice({super.key});

  @override
  State<SelectBLEDevice> createState() => _SelectBLEDeviceState();
}

class _SelectBLEDeviceState extends State<SelectBLEDevice> {
  SelectDeviceController _selectDeviceController = SelectDeviceController();

  static const platform = MethodChannel('com.example.rfid_flutter');

  static const EventChannel scanChannel =
      EventChannel('com.example.rfid_flutter/scan');

  // static const MethodChannel methodChannel =
  //     MethodChannel('com.example.rfid_flutter/command');

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    _selectDeviceController.askPermissions();
    //  platform.invokeMethod('isBluetoothSupported');
    _isBluetoothSupported();

    scanChannel.receiveBroadcastStream().listen(_onEvent, onError: _onError);

    _createProfile(generateUniqueProfileName());
  }

  String generateUniqueProfileName() {
    const allowedChars =
        'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const profileNameLength = 5; // Adjust as needed

    String generatedName = '';
    for (int i = 0; i < profileNameLength; i++) {
      generatedName += allowedChars[Random().nextInt(allowedChars.length)];
    }

    print("Generated Name: $generatedName");
    // if (!existingNames.contains(generatedName)) {
    return generatedName;
    // }
  }

  Future<void> _sendDataWedgeCommand(String command, String parameter) async {
    try {
      _selectDeviceController.sendingDataWedgeCommand.value = true;

      String argumentAsJson = "{\"command\":$command,\"parameter\":$parameter}";
      await platform.invokeMethod('sendCommandString', argumentAsJson);

      _selectDeviceController.sendingDataWedgeCommand.value = false;
    } catch (e) {
      if (e is PlatformException) {
        print('PlatformException occurred: ${e.code} - ${e.message}');
      } else {
        print("Error invoking Android method ${e.toString()}");
      }

      _selectDeviceController.sendingDataWedgeCommand.value = false;
    }
  }

  Future<void> _isBluetoothSupported() async {
    try {
      await platform.invokeMethod('isBluetoothSupported');
    } catch (e) {
      if (e is PlatformException) {
        print('PlatformException occurred: ${e.code} - ${e.message}');
      } else {
        print("Error invoking Android method ${e.toString()}");
      }
    }
  }

  Future<void> _createProfile(String profileName) async {
    try {
      await platform.invokeMethod('createProfile', profileName);
    } catch (e) {
      //  Error invoking Android method
      if (e is PlatformException) {
        print('PlatformException occurred: ${e.code} - ${e.message}');
      } else {
        print("Error invoking Android method ${e.toString()}");
      }
      // print("Error invoking Android method ${e.toString()}");
    }
  }

  // String _barcodeString = "Barcode will be shown here";
  // String _barcodeSymbology = "Symbology will be shown here";
  // String _scanTime = "Scan Time will be shown here";

  void _onEvent(dynamic event) {
    Map barcodeScan = jsonDecode(event.toString());
    _selectDeviceController.barcodeString.value =
        "Barcode: ${barcodeScan['scanData']}";

    _selectDeviceController.barcodeSymbology.value =
        "Symbology: ${barcodeScan['symbology']}";

    _selectDeviceController.scanTime.value = "At: ${barcodeScan['dateTime']}";

    // _barcodeString = "Barcode: " + barcodeScan['scanData'];
    // _barcodeSymbology = "Symbology: " + barcodeScan['symbology'];
    // _scanTime = "At: " + barcodeScan['dateTime'];
  }

  void _onError(Object error) {
    _selectDeviceController.barcodeString.value = "Barcode: error";
    _selectDeviceController.barcodeSymbology.value = "Symbology: error";
    _selectDeviceController.scanTime.value = "At: error";
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title:
            const Text('Select BLE Device', style: TextStyle(fontSize: 18.0)),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Obx(
            () => Center(
                child: _selectDeviceController.pairingDevices.value
                    ? SizedBox(
                        height: 50.0,
                        width: 50.0,
                        child: CircularProgressIndicator(
                          color: Colors.blue,
                        ),
                      )
                    : ElevatedButton(
                        onPressed: () async {
                          await platform.invokeMethod('getPairedDevices');
                        },
                        child: Text('Connect to RFID scanner'))),
          ),
          SizedBox(height: 20.0),
          Obx(
            () => Center(
                child:
                    // false
                    _selectDeviceController.sendingDataWedgeCommand.value
                        ? SizedBox(
                            height: 50.0,
                            width: 50.0,
                            child: CircularProgressIndicator(
                              color: Colors.blue,
                            ),
                          )
                        : ElevatedButton(
                            onPressed: () async {
                              await _sendDataWedgeCommand(
                                  "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER",
                                  "START_SCANNING");
                            },
                            child: Text('Start scan'))),
          ),
          SizedBox(height: 20.0),
          Obx(
            () => Center(
                child:
                    // false
                    _selectDeviceController.sendingDataWedgeCommand.value
                        ? SizedBox(
                            height: 50.0,
                            width: 50.0,
                            child: CircularProgressIndicator(
                              color: Colors.blue,
                            ),
                          )
                        : ElevatedButton(
                            onPressed: () async {
                              await _sendDataWedgeCommand(
                                  "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER",
                                  "STOP_SCANNING");
                            },
                            child: Text('Stop scan'))),
          ),
          SizedBox(height: 20.0),
          Text(
            'Barcode: ${_selectDeviceController.barcodeString.value}',
            style: TextStyle(fontSize: 18.0),
          ),
          SizedBox(height: 20.0),
          Text(
            'Symbology: ${_selectDeviceController.barcodeSymbology.value}',
            style: TextStyle(fontSize: 18.0),
          ),
          SizedBox(height: 20.0),
          Text(
            'Scan Time: ${_selectDeviceController.scanTime.value}',
            style: TextStyle(fontSize: 18.0),
          ),
        ],
      ),
    );
  }
}
