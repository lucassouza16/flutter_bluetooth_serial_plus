package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FlutterBluetoothSerialPlusService {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String TAG = FlutterBluetoothSerialPlusService.class.getSimpleName();

    public FlutterBluetoothSerialPlusService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public List<BluetoothDevice> list() {

        List<BluetoothDevice> devices = new ArrayList();

        if (!hasAdapter()) {
            Log.d(TAG, "No bluetooth adapter available");
            return devices;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
            }
        }

        return devices;
    }

    public boolean connect(BluetoothDevice device) throws IOException {

        if (mmDevice != null) return false;

        mmSocket = device.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        mmDevice = device;

        return true;
    }

    public BluetoothDevice findDevice(String address) {
        List<BluetoothDevice> devices = list();

        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(address)) {
                return device;
            }
        }

        return null;
    }

    public void write(byte[] bytes) throws IOException {

        if (mmDevice == null) return;

        mmOutputStream.write(bytes);
    }

    public void disconnect() throws IOException {

        if (mmDevice == null) return;

        mmDevice = null;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    public boolean hasAdapter() {
        return mBluetoothAdapter != null;
    }

    public boolean bluetoothEnabled() {
        if (!hasAdapter()) {
            return false;
        }

        return mBluetoothAdapter.isEnabled();
    }

    public BluetoothDevice connectedDevice() {
        return this.mmDevice;
    }

    public byte[] read() throws IOException {
        if (mmDevice == null) return null;

        byte[] buffer = new byte[1024];
        int bytesRead = mmInputStream.read(buffer);

        if (bytesRead > 0) {
            byte[] received = new byte[bytesRead];
            System.arraycopy(buffer, 0, received, 0, bytesRead);

            return received;
        }

        return null;
    }
}
