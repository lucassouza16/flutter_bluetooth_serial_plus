import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';

class FlutterBluetoothSerialPlus {
  FlutterBluetoothSerialPlus() {
    methodChannel.setMethodCallHandler(nativeMethodCallHandler);
  }

  final methodChannel = const MethodChannel('flutter_bluetooth_serial_plus');

  static final FlutterBluetoothSerialPlus instance = FlutterBluetoothSerialPlus();

  final StreamController<BluetoothStateEvent> _state = StreamController.broadcast();

  Future<dynamic> nativeMethodCallHandler(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'onStateChange':
        _state.add(BluetoothStateEvent.fromMap(methodCall.arguments));
        break;
    }
  }

  Future<List<BluetoothDevice>> scanDevices() async {
    final devices = await methodChannel.invokeMethod<List<dynamic>>('scanDevices');

    return BluetoothDevice.fromListMap(devices ?? []);
  }

  Future<bool> connect(BluetoothDevice device) async {
    return await methodChannel.invokeMethod<bool>('connect', device.address) ?? false;
  }

  Future<bool> disconnect() async {
    return await methodChannel.invokeMethod<bool>('disconnect') ?? false;
  }

  Future<bool> write(Uint8List bytes) async {
    return await methodChannel.invokeMethod<bool>('write', bytes) ?? false;
  }

  Future<bool> checkPermissions() async {
    return await methodChannel.invokeMethod<bool>('checkPermissions') ?? false;
  }

  Future<bool> requestPermissions() async {
    return await methodChannel.invokeMethod<bool>('requestPermissions') ?? false;
  }

  Future<bool> bluetoothEnabled() async {
    return await methodChannel.invokeMethod<bool>('bluetoothEnabled') ?? false;
  }

  Future<bool> enableBluetooth() async {
    return await methodChannel.invokeMethod<bool>('enableBluetooth') ?? false;
  }

  Future<BluetoothDevice?> connectedDevice() async {
    var device = await methodChannel.invokeMethod<dynamic>('connectedDevice');

    if (device != null) {
      return BluetoothDevice.fromMap(device);
    } else {
      return null;
    }
  }

  Stream<BluetoothStateEvent> get state => _state.stream;
}
