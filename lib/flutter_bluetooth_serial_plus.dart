import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';

class FlutterBluetoothSerialPlus {
  final methodChannel = const MethodChannel('flutter_bluetooth_serial_plus');

  static final FlutterBluetoothSerialPlus instance = FlutterBluetoothSerialPlus();

  final EventChannel _state = const EventChannel("flutter_bluetooth_serial_plus/stateChannel");
  final EventChannel _readData = const EventChannel("flutter_bluetooth_serial_plus/readDataChannel");

  Future<List<BluetoothDevice>> get listDevices async {
    final devices = await methodChannel.invokeMethod<List<dynamic>>('listDevices');

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

  Future<bool> get hasPermissions async => await methodChannel.invokeMethod<bool>('hasPermissions') ?? false;

  Future<bool> requestPermissions() async {
    return await methodChannel.invokeMethod<bool>('requestPermissions') ?? false;
  }

  Future<bool> get isBluetoothEnabled async => await methodChannel.invokeMethod<bool>('isBluetoothEnabled') ?? false;

  Future<bool> enableBluetooth() async {
    return await methodChannel.invokeMethod<bool>('enableBluetooth') ?? false;
  }

  Future<BluetoothDevice?> get connectedDevice async {
    var device = await methodChannel.invokeMethod<dynamic>('connectedDevice');

    if (device != null) {
      return BluetoothDevice.fromMap(device);
    } else {
      return null;
    }
  }

  Stream<BluetoothStateEvent> get state => _state.receiveBroadcastStream().map((event) => BluetoothStateEvent.fromMap(event));

  Stream<Uint8List> get read => _readData.receiveBroadcastStream().map((event) => event as Uint8List);
}
