import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _bluetoothPlugin = FlutterBluetoothSerialPlus.instance;
  List<BluetoothDevice> _devices = [];
  BluetoothDevice? _connected;

  @override
  void initState() {
    super.initState();

    _bluetoothPlugin.state.listen(
      (event) {
        switch (event.state) {
          case BluetoothStateEvent.connect:
            print('Connected device');
            setState(() => _connected = event.device);
            break;
          case BluetoothStateEvent.disconnect:
            print('Disconnected device');
            setState(() => _connected = null);
            break;
          case BluetoothStateEvent.on:
            print('Bluetooth on');
            break;
          case BluetoothStateEvent.off:
            print('Bluetooth off');
            setState(() => _connected = null);
            break;
        }
      },
    );

    initScanDevices();
  }

  Future<void> initScanDevices() async {
    if (!await _bluetoothPlugin.hasPermissions) {
      _bluetoothPlugin.requestPermissions();

      if (!await _bluetoothPlugin.isBluetoothEnabled) {
        print(await _bluetoothPlugin.enableBluetooth());
      }
    }

    var devices = await _bluetoothPlugin.listDevices;

    setState(() {
      _devices = devices;
    });
  }

  Future<void> connectDevice(BluetoothDevice device) async {
    if (await _bluetoothPlugin.connect(device)) {
      print("Connection successfull");
    } else {
      print("Connection failed");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(children: [
          Expanded(
            child: ListView.builder(
              itemCount: _devices.length,
              itemBuilder: (context, index) {
                var item = _devices[index];

                return ListTile(
                  title: Text(item.name),
                  subtitle: Text(item.address),
                  selected: _connected == item,
                  onTap: () => connectDevice(item),
                );
              },
            ),
          )
        ]),
        floatingActionButton: FloatingActionButton(
          child: const Icon(Icons.print),
          onPressed: () async {
            // await _bluetoothPlugin.disconnect();
            print(await _bluetoothPlugin.write(Uint8List.fromList([
              //Disable chinese char
              0x1C,
              0x2E,
              0x1B,
              0x74,
              0x10,
              //font, size, etc
              0x1B, 0x21, 0x08,
              0x1b, 0x4d, 0x00,
              0x1B, 0x61, 0x01,
              //text
              ...latin1.encode('AÃeéÕõçÇ'),
              //feed line
              0x0A,
            ])));
          },
        ),
      ),
    );
  }
}
