import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';
import 'package:flutter_bluetooth_serial_plus_example/widgets/devicelistitem.widget.dart';
import 'package:flutter_bluetooth_serial_plus_example/pages/chat.page.dart';
import 'package:flutter_bluetooth_serial_plus_example/widgets/messagelistitem.widget.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _bluetoothPlugin = FlutterBluetoothSerialPlus.instance;
  List<BluetoothDevice> _devices = [];
  BluetoothDevice? _connecting;
  BluetoothDevice? _connected;

  final ValueNotifier<List<MessageModel>> _messages = ValueNotifier([]);

  @override
  void initState() {
    super.initState();

    _bluetoothPlugin.connectedDevice.then((value) {
      setState(() => _connected = value);
    });

    String buff = "";

    _bluetoothPlugin.read.listen((event) {
      String message = utf8.decode(event);
      bool endRead = false;

      buff += message;

      if (buff.contains(RegExp(r'\n$'))) {
        endRead = true;
        buff = buff.replaceFirst(RegExp(r'\n$'), '');
      }

      if (endRead) {
        _messages.value = [
          MessageModel(
            message: buff,
            time: DateTime.now(),
            type: MessageModel.isOther,
          ),
          ..._messages.value,
        ];
        buff = "";
      }
    });

    _bluetoothPlugin.state.listen(
      (event) {
        switch (event.state) {
          //If state is connect or disconnect, device bluettoth on event is not null
          case BluetoothStateEvent.connect:
            debugPrint('Connected device: ${event.device!.name}');
            _messages.value = [
              MessageModel(message: "Device ${event.device!.name} has connected", type: MessageModel.isInfo, time: DateTime.now()),
              ..._messages.value,
            ];
            setState(() {
              _connecting = null;
              _connected = event.device;
            });
            break;
          case BluetoothStateEvent.disconnect:
            debugPrint('Disconnected device: ${event.device!.name}');
            _messages.value = [
              MessageModel(message: "Device ${event.device!.name} has disconnected", type: MessageModel.isInfo, time: DateTime.now()),
              ..._messages.value,
            ];
            setState(() {
              _connected = null;
            });
            break;
          case BluetoothStateEvent.on:
            debugPrint('Bluetooth on');
            break;
          case BluetoothStateEvent.off:
            debugPrint('Bluetooth off');
            _messages.value = [
              MessageModel(message: "Bluetooth device turn off, connection lost", type: MessageModel.isInfo, time: DateTime.now()),
              ..._messages.value,
            ];
            setState(() {
              _connecting = null;
              _connected = null;
            });
            break;
        }
      },
    );

    _initScanDevices();
  }

  Future<void> _initScanDevices() async {
    if (!await _bluetoothPlugin.hasPermissions) {
      debugPrint("No bluetooth permission conceded, requesting...");

      if (!await _bluetoothPlugin.requestPermissions()) {
        debugPrint("User deny bluetooth permissions...");
        return;
      }
    }

    if (!await _bluetoothPlugin.isBluetoothEnabled) {
      debugPrint("Bluetooth device disabled, trying enable...");

      if (!await _bluetoothPlugin.enableBluetooth()) {
        debugPrint("User deny bluetooth on...");
        return;
      }
    }

    var devices = await _bluetoothPlugin.listDevices;

    setState(() {
      _devices = devices;
    });
  }

  Future<void> connectDevice(BluetoothDevice device) async {
    if (_connected != null) {
      final scaffold = ScaffoldMessenger.of(context);
      scaffold.showSnackBar(
        const SnackBar(
          content: Text('Device already connected'),
        ),
      );

      return;
    }

    setState(() => _connecting = device);

    if (await _bluetoothPlugin.connect(device)) {
      debugPrint("Connection successfull");
      if (mounted) {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => ChatPage(messages: _messages)),
        );
      }
    } else {
      debugPrint("Connection failed");
      setState(() => _connecting = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Connect to device'),
      ),
      body: RefreshIndicator(
        onRefresh: _initScanDevices,
        child: ListView.builder(
          itemCount: _devices.length,
          itemBuilder: (context, index) {
            var item = _devices[index];

            return DeviceListItem(
              item: item,
              connecting: _connecting,
              connected: _connected,
              onSelect: (item) => connectDevice(item),
            );
          },
        ),
      ),
      floatingActionButton: _connected != null
          ? FloatingActionButton(
              child: const Icon(Icons.messenger_outline_outlined),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => ChatPage(messages: _messages)),
                );
              },
            )
          : null,
    );
  }
}
