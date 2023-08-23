package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlutterBluetoothSerialPlusFunctions {
    private FlutterBluetoothSerialPlusService service = new FlutterBluetoothSerialPlusService();

    public List<Map<String, Object>> scanDevices () {
        List<BluetoothDevice> devices = service.list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (BluetoothDevice device: devices) {
            Map<String, Object> item = new HashMap<>();

            boolean connected = service.mmDevice != null &&
                    service.mmDevice.getAddress().equals(device.getAddress());

            item.put("name", device.getName());
            item.put("address", device.getAddress());
            item.put("connected", connected);

            result.add(item);
        }

        return result;
    }

    public boolean connect(String address) {
        BluetoothDevice device = service.findDevice(address);

        if(device == null) {
            return false;
        } else {
            try {
                service.connect(device);

                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public boolean disconnect() {
        try{
            service.disconnect();

            return true;
        } catch (IOException e){
            return false;
        }
    }

    public boolean write(byte[] bytes) {
        try{
            service.write(bytes);

            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
}
