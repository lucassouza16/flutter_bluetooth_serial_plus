import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';

class FlutterBluetoothSerialPlus {
  final methodChannel = const MethodChannel('flutter_bluetooth_serial_plus');

  static final FlutterBluetoothSerialPlus instance = FlutterBluetoothSerialPlus();

  final EventChannel _state = const EventChannel("flutter_bluetooth_serial_plus/stateChannel");
  final EventChannel _readData = const EventChannel("flutter_bluetooth_serial_plus/readDataChannel");

  /// Future which will return the already connected bluetooth devices
  Future<List<BluetoothDevice>> get listDevices async {
    final devices = await methodChannel.invokeMethod<List<dynamic>>('listDevices');

    return BluetoothDevice.fromListMap(devices ?? []);
  }

  /// Pair to bluetooth device
  ///
  /// Returns `true` if connection has been successfull
  Future<bool> connect(BluetoothDevice device) async {
    return await methodChannel.invokeMethod<bool>('connect', device.address) ?? false;
  }

  /// Disconnect to bluetooth device
  ///
  /// Returns `true` if disconnection has been successfull
  Future<bool> disconnect() async {
    return await methodChannel.invokeMethod<bool>('disconnect') ?? false;
  }

  ///Send byte array to connected device
  ///
  ///Returns `true` if bytes has been sended
  Future<bool> write(Uint8List bytes) async {
    return await methodChannel.invokeMethod<bool>('write', bytes) ?? false;
  }

  ///Future to know if the application has the necessary bluetooth permissions
  ///
  ///Returns `true` if permissions were conceded
  Future<bool> get hasPermissions async => await methodChannel.invokeMethod<bool>('hasPermissions') ?? false;

  ///Future to request user for the necessary bluetooth permissions
  ///
  ///Returns `true` if permissions were conceded
  Future<bool> requestPermissions() async {
    return await methodChannel.invokeMethod<bool>('requestPermissions') ?? false;
  }

  ///Future to determine if the device has bluetooth service and is active
  ///
  ///Returns `true` if device has bluetooth service and is active
  Future<bool> get isBluetoothEnabled async => await methodChannel.invokeMethod<bool>('isBluetoothEnabled') ?? false;

  ///Future to enable device bluetooth service programmatically
  ///
  ///Returns `true` if device has bluetooth service and is active
  Future<bool> enableBluetooth() async {
    return await methodChannel.invokeMethod<bool>('enableBluetooth') ?? false;
  }

  ///which returns the currently paired bluetooth device
  ///
  ///Returns [BluetoothDevice] if if there is a paired device
  Future<BluetoothDevice?> get connectedDevice async {
    var device = await methodChannel.invokeMethod<dynamic>('connectedDevice');

    if (device != null) {
      return BluetoothDevice.fromMap(device);
    } else {
      return null;
    }
  }

  ///Listen to determine current connection status
  Stream<BluetoothStateEvent> get state => _state.receiveBroadcastStream().map((event) => BluetoothStateEvent.fromMap(event));

  ///Listen to bytes data send from actual paired device
  Stream<Uint8List> get read => _readData.receiveBroadcastStream().map((event) => event as Uint8List);
}
