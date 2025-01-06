import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

class SelectDeviceController extends GetxController {
  Future askPermissions() async {
    // Permission.bluetooth.request();
    // Permission.location.request();

    Map<Permission, PermissionStatus> statuses = await [
      Permission.bluetooth,
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.location,
    ].request();

    print("Permission Statuses:");
    statuses.forEach(
      (key, value) {
        print('$key: $value');
      },
    );
  }

  scanDevices() async {
    if (await Permission.bluetoothScan.request().isGranted &&
        await Permission.bluetoothConnect.request().isGranted) {
      print("Scanning for devices...");
    } else {
      print("Permission denied");
      Get.snackbar(
        "Permission Denied",
        "Please enable Bluetooth permissions",
        snackPosition: SnackPosition.BOTTOM,
      );
    }
  }

  RxBool pairingDevices = false.obs;

  RxString barcodeString = "Barcode will be shown here".obs;
  RxString barcodeSymbology = "Symbology will be shown here".obs;
  RxString scanTime = "Scan Time will be shown here".obs;

  RxBool sendingDataWedgeCommand = false.obs;
}
