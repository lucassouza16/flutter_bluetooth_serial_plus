package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;

public class FlutterBluetoothSerialPlusMapUtils {
    static Map<String, Object> bluetoothDeviceToMap(BluetoothDevice device) {
        Map hash = new HashMap<>();

        hash.put("name", device.getName());
        hash.put("address", device.getAddress());

        return hash;
    }

    static Map<String, Object> bluetoothStateToMap(int state, BluetoothDevice device) {
        Map hash = new HashMap<>();

        hash.put("state", state);
        hash.put("device", device == null ? null : bluetoothDeviceToMap(device));

        return hash;
    }
}
