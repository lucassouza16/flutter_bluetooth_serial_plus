import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus_models.dart';

class DeviceListItem extends StatelessWidget {
  final BluetoothDevice item;
  final BluetoothDevice? connecting;
  final BluetoothDevice? connected;
  final Function(BluetoothDevice item) onSelect;

  const DeviceListItem({
    super.key,
    required this.item,
    required this.onSelect,
    this.connecting,
    this.connected,
  });

  @override
  Widget build(BuildContext context) {
    bool isConnecting = connecting == item;
    bool isConnected = connected == item;

    return InkWell(
      onTap: () {
        if (connecting != null) return;

        onSelect(item);
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 10),
        child: Row(children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.name,
                  style: TextStyle(
                    color: isConnected ? Colors.blue : null,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  item.address,
                  style: TextStyle(
                    color: isConnected ? Colors.blue : null,
                  ),
                ),
              ],
            ),
          ),
          if (isConnecting)
            const SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(
                strokeWidth: 2,
              ),
            ),
        ]),
      ),
    );
  }
}
