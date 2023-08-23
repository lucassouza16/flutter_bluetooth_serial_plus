package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FlutterBluetoothSerialPlusService {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;

    Thread workerThread;

    boolean isConnected = false;

    private static final String TAG = FlutterBluetoothSerialPlusService.class.getSimpleName();

    public FlutterBluetoothSerialPlusService() {
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if(isConnected && mBluetoothAdapter.getState() != BluetoothAdapter.STATE_OFF) {
                         try{
                             disconnect();
                             Log.d(TAG, "Connection to device lost, closing...");
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                    }
                }
            }
        });

        workerThread.start();
    }

    public List<BluetoothDevice> list() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "No bluetooth adapter available");
        }

        // if(!mBluetoothAdapter.isEnabled()) {
        //     Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //     startActivityForResult(enableBluetooth, 0);
        //  }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> devices = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
            }
        }

        return devices;
    }

    public void connect(BluetoothDevice device) throws IOException {

        if (isConnected) return;

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        mmDevice = device;
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        isConnected = true;
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
        mmOutputStream.write(bytes);
    }

    public void disconnect() throws IOException {
        isConnected = false;
        mmDevice = null;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }
}
