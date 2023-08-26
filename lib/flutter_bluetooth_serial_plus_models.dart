class BluetoothDevice {
  final String name;
  final String address;

  const BluetoothDevice({
    required this.name,
    required this.address,
  });

  @override
  int get hashCode => address.hashCode;

  @override
  bool operator ==(other) {
    return other is BluetoothDevice && other.address == address;
  }

  factory BluetoothDevice.fromMap(dynamic map) => BluetoothDevice(
        name: map['name'],
        address: map['address'],
      );

  static List<BluetoothDevice> fromListMap(List<dynamic> list) => list
      .map(
        (map) => BluetoothDevice.fromMap(map),
      )
      .toList();

  dynamic toMap() => {
        'name': name,
        'address': address,
      };
}

class BluetoothStateEvent {
  static const int connect = 0;
  static const int disconnect = 1;
  static const int off = 2;
  static const int on = 3;

  ///1. [BluetoothStateEvent.on] If bluetooth service was turn on
  ///2. [BluetoothStateEvent.off] If bluetooth service was turn off
  ///3. [BluetoothStateEvent.connect] If bluetooth device was connected
  ///4. [BluetoothStateEvent.disconnect] If bluetooth device was disconnected
  final int state;

  ///If status for state is [BluetoothStateEvent.on] or [BluetoothStateEvent.off], device is not null
  final BluetoothDevice? device;

  BluetoothStateEvent({
    required this.state,
    this.device,
  });

  factory BluetoothStateEvent.fromMap(dynamic map) => BluetoothStateEvent(
        state: map['state'],
        device: map['device'] == null ? null : BluetoothDevice.fromMap(map['device']),
      );

  dynamic toMap() => {
        'state': state,
        'device': device?.toMap(),
      };
}
