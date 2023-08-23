class BluetoothDevice {
  final String name;
  final String address;
  final bool connected;

  const BluetoothDevice({
    required this.name,
    required this.address,
    required this.connected,
  });

  factory BluetoothDevice.fromMap(dynamic map) => BluetoothDevice(
        name: map['name'],
        address: map['address'],
        connected: map['connected'],
      );

  static List<BluetoothDevice> fromListMap(List<dynamic> list) => list
      .map(
        (map) => BluetoothDevice.fromMap(map),
      )
      .toList();

  dynamic toMap() => {
        'name': name,
        'address': address,
        'connected': connected,
      };
}
