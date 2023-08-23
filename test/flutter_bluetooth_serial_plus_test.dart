import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_platform_interface.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterBluetoothSerialPlusPlatform
    with MockPlatformInterfaceMixin
    implements FlutterBluetoothSerialPlusPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterBluetoothSerialPlusPlatform initialPlatform = FlutterBluetoothSerialPlusPlatform.instance;

  test('$MethodChannelFlutterBluetoothSerialPlus is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterBluetoothSerialPlus>());
  });

  test('getPlatformVersion', () async {
    FlutterBluetoothSerialPlus flutterBluetoothSerialPlusPlugin = FlutterBluetoothSerialPlus();
    MockFlutterBluetoothSerialPlusPlatform fakePlatform = MockFlutterBluetoothSerialPlusPlatform();
    FlutterBluetoothSerialPlusPlatform.instance = fakePlatform;

    expect(await flutterBluetoothSerialPlusPlugin.getPlatformVersion(), '42');
  });
}
