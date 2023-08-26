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

        if (mBluetoothAdapter == null) {
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

    public boolean bluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            return false;
        }

        return mBluetoothAdapter.isEnabled();
    }

    public BluetoothDevice connectedDevice() {
        return this.mmDevice;
    }

    @FunctionalInterface
    public interface EnableBluetoothCallback {
        void execute(boolean enabled);
    }

    public void enableBluetooth(Activity activity, EnableBluetoothCallback callback) {

        if (mBluetoothAdapter == null) {
            callback.execute(false);
            return;
        }

        final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {

                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);

                    if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF) {
                        activity.unregisterReceiver(this);
                        callback.execute(state == BluetoothAdapter.STATE_ON);
                    }
                } else if(FlutterBluetoothSerialPlusPlugin.ACTION_FLUTTER_ACTIVITY_RESULT.equals(intent.getAction())) {

                    int requestCode = intent.getIntExtra("requestCode", -1);
                    int resultCode = intent.getIntExtra("resultCode", -1);

                    if(requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
                        activity.unregisterReceiver(this);
                        callback.execute(false);
                    }
                }
            }
        };

        activity.registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        activity.registerReceiver(bluetoothStateReceiver, new IntentFilter(FlutterBluetoothSerialPlusPlugin.ACTION_FLUTTER_ACTIVITY_RESULT));

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, 1);
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
