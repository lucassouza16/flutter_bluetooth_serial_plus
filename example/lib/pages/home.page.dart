import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';
import 'package:flutter_bluetooth_serial_plus_example/widgets/devicelistitem.widget.dart';
import 'package:flutter_bluetooth_serial_plus_example/pages/chat.page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _bluetoothPlugin = FlutterBluetoothSerialPlus.instance;
  List<BluetoothDevice> _devices = [];
  BluetoothDevice? _connecting;

  @override
  void initState() {
    super.initState();

    _bluetoothPlugin.state.listen(
      (event) {
        switch (event.state) {
          //If state is connect or disconnect, device bluettoth on event is not null
          case BluetoothStateEvent.connect:
            debugPrint('Connected device: ${event.device}');
            break;
          case BluetoothStateEvent.disconnect:
            debugPrint('Disconnected device: ${event.device}');
            setState(() => _connecting = null);
            break;
          case BluetoothStateEvent.on:
            debugPrint('Bluetooth on');
            break;
          case BluetoothStateEvent.off:
            debugPrint('Bluetooth off');
            setState(() => _connecting = null);
            break;
        }
      },
    );

    initScanDevices();
  }

  Future<void> initScanDevices() async {
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
    setState(() => _connecting = device);

    if (await _bluetoothPlugin.connect(device)) {
      debugPrint("Connection successfull");
      if (mounted) {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => const ChatPage()),
        );
      }
    } else {
      setState(() => _connecting = null);
      debugPrint("Connection failed");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Connect to device'),
      ),
      body: Column(children: [
        Expanded(
          child: ListView.builder(
            itemCount: _devices.length,
            itemBuilder: (context, index) {
              var item = _devices[index];

              return DeviceListItem(
                item: item,
                connecting: _connecting,
                onSelect: (item) => connectDevice(item),
              );
            },
          ),
        )
      ]),
    );
  }
}
