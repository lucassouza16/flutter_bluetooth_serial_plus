package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlutterBluetoothSerialPlusFunctions {
    private FlutterBluetoothSerialPlusService service = new FlutterBluetoothSerialPlusService();

    public List<Map<String, Object>> listDevices() {
        List<BluetoothDevice> devices = service.list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (BluetoothDevice device: devices) {
            result.add(FlutterBluetoothSerialPlusMapUtils.bluetoothDeviceToMap(device));
        }

        return result;
    }

    public boolean connect(String address) {
        BluetoothDevice device = service.findDevice(address);

        if(device == null) {
            return false;
        } else {
            try {
                return service.connect(device);
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

    public boolean hasPermissions(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 2);
        }

        return true;
    }

    public Map<String, Object> connectedDevice(){
        BluetoothDevice actual = service.connectedDevice();

        if(actual == null) return null;

        return FlutterBluetoothSerialPlusMapUtils.bluetoothDeviceToMap(actual);
    }

    public boolean isBluetoothEnabled(){
        return service.bluetoothEnabled();
    }

    public void enableBluetooth (Activity activity, FlutterBluetoothSerialPlusService.EnableBluetoothCallback callback){
        service.enableBluetooth(activity, callback);
    }
}
