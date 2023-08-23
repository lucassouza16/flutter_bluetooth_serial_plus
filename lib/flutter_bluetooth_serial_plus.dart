import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';

class FlutterBluetoothSerialPlus {
  final methodChannel = const MethodChannel('flutter_bluetooth_serial_plus');

  static final FlutterBluetoothSerialPlus instance = FlutterBluetoothSerialPlus();

  Future<List<BluetoothDevice>> scanDevices() async {
    final devices = await methodChannel.invokeMethod<List<dynamic>>('scanDevices');

    return BluetoothDevice.fromListMap(devices ?? []);
  }

  Future<bool> connect(BluetoothDevice device) async {
    return await methodChannel.invokeMethod<bool>('connect', device.address) ?? false;
  }

  Future<bool> write(Uint8List bytes) async {
    return await methodChannel.invokeMethod<bool>('write', bytes) ?? false;
  }
}
