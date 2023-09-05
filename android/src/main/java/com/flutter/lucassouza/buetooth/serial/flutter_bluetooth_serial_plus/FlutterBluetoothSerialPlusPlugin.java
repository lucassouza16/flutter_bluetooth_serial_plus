package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterBluetoothSerialPlusPlugin implements
        FlutterPlugin,
        ActivityAware,
        MethodCallHandler,
        PluginRegistry.ActivityResultListener,
        PluginRegistry.RequestPermissionsResultListener {
    Context context;
    private MethodChannel channel;
    private EventChannel eventStateChannel;
    private EventChannel eventReadDataChannel;
    private Activity activity;
    private FlutterBluetoothSerialPlusService service = new FlutterBluetoothSerialPlusService();

    private Result requestPermissionsResult;
    private Result enableBluetoothResult;

    private static final String TAG = FlutterBluetoothSerialPlusPlugin.class.getSimpleName();

    private final EventChannel.StreamHandler stateStreamHandler = new EventChannel.StreamHandler() {

        private EventChannel.EventSink sink;
        BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                    if (state == BluetoothAdapter.STATE_OFF) {
                        disconnect(null);

                        sink.success(
                                FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(2, null)
                        );
                    } else if (state == BluetoothAdapter.STATE_ON) {
                        sink.success(
                                FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(3, null)
                        );
                    }
                } else {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        Log.d(TAG, "Device found");
                    } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                        Log.d(TAG, "Device connected");
                        sink.success(
                                FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(0, device)
                        );
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        Log.d(TAG, "Done searching");
                    } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                        Log.d(TAG, "Device is about to disconnect");
                    } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                        Log.d(TAG, "Device has disconnect");
                        disconnect(null);

                        sink.success(
                                FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(1, device)
                        );
                    }
                }
            }
        };

        @Override
        public void onListen(Object o, EventChannel.EventSink eventSink) {
            sink = eventSink;
            context.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            context.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
            context.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            context.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }

        @Override
        public void onCancel(Object o) {
            sink = null;
            context.unregisterReceiver(bluetoothDeviceReceiver);
        }
    };

    private final EventChannel.StreamHandler readDataStreamHandler = new EventChannel.StreamHandler() {

        private EventChannel.EventSink sink;

        Thread workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (sink != null) {
                    byte[] readData = null;

                    try {
                        readData = service.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (readData != null) {
                        byte[] finalReadData = readData;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                sink.success(finalReadData);
                            }
                        });
                    }
                }
            }
        });

        @Override
        public void onListen(Object o, EventChannel.EventSink eventSink) {
            sink = eventSink;

            if (!workerThread.isAlive()) {
                workerThread.start();
            }
        }

        @Override
        public void onCancel(Object o) {
            sink = null;
        }
    };

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF) {
                    if(enableBluetoothResult != null) {
                        enableBluetoothResult.success(state == BluetoothAdapter.STATE_ON);
                        enableBluetoothResult = null;
                    }
                }
            }
        }
    };

    @Override
    public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
        //Log.d(TAG, "onAttachedToEngine");

        context = flutterPluginBinding.getApplicationContext();

        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bluetooth_serial_plus");
        eventStateChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bluetooth_serial_plus/stateChannel");
        eventReadDataChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bluetooth_serial_plus/readDataChannel");

        eventStateChannel.setStreamHandler(stateStreamHandler);
        eventReadDataChannel.setStreamHandler(readDataStreamHandler);

        channel.setMethodCallHandler(this);
    }

    private void requestPermissions(Result result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            result.success(true);
        } else {
            requestPermissionsResult = result;

            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 2);
        }
    }

    public void enableBluetooth(Result result) {
        if (!service.hasAdapter()) {
            result.success(false);
            return;
        }

        enableBluetoothResult = result;

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, 1);
    }

    public void listDevices(Result result) {
        List<BluetoothDevice> devices = service.list();
        List<Map<String, Object>> devicesMap = new ArrayList<>();

        for (BluetoothDevice device: devices) {
            devicesMap.add(FlutterBluetoothSerialPlusMapUtils.bluetoothDeviceToMap(device));
        }

        result.success(devicesMap);
    }

    public void connect(MethodCall call, Result result) {
        BluetoothDevice device = service.findDevice(call.arguments());

        if(device == null) {
            result.success(false);
        } else {
            try {
                result.success(service.connect(device));
            } catch (IOException e) {
                result.success(false);
            }
        }
    }

    public void disconnect(Result result) {
        try{
            service.disconnect();

            if(result != null) result.success(true);
        } catch (IOException e){
            if(result != null) result.success(false);
        }
    }

    public void write(MethodCall call, Result result) {
        try{
            service.write(call.arguments());

            result.success(true);
        }catch (IOException e){
            e.printStackTrace();
            result.success(false);
        }
    }

    public void hasPermissions(Result result) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            result.success(true);
            return;
        }

        result.success(ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
    }

    public void isBluetoothEnabled(Result result){
        result.success(service.bluetoothEnabled());
    }

    public void connectedDevice(Result result){
        BluetoothDevice actual = service.connectedDevice();

        if(actual == null) {
            result.success(null);
            return;
        }

        result.success(FlutterBluetoothSerialPlusMapUtils.bluetoothDeviceToMap(actual));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("listDevices")) {
            listDevices(result);
        } else if (call.method.equals("connect")) {
            connect(call, result);
        } else if (call.method.equals("disconnect")) {
            disconnect(result);
        } else if (call.method.equals("write")) {
            write(call, result);
        } else if (call.method.equals("hasPermissions")) {
            hasPermissions(result);
        } else if (call.method.equals("isBluetoothEnabled")) {
            isBluetoothEnabled(result);
        } else if (call.method.equals("requestPermissions")) {
            requestPermissions(result);
        } else if (call.method.equals("enableBluetooth")) {
            enableBluetooth(result);
        } else if (call.method.equals("connectedDevice")) {
            connectedDevice(result);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        //Log.d(TAG, "onAttachedToActivity");

        activity = activityPluginBinding.getActivity();

        activity.registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        activityPluginBinding.addActivityResultListener(this);
        activityPluginBinding.addRequestPermissionsResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {

    }

    @Override
    public void onDetachedFromActivity() {
        activity.unregisterReceiver(bluetoothStateReceiver);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        if(requestCode == 1) {
            if(enableBluetoothResult != null) {
                enableBluetoothResult.success(resultCode != Activity.RESULT_CANCELED);
                enableBluetoothResult = null;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 2) {
            if(requestPermissionsResult != null) {
                requestPermissionsResult.success(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                requestPermissionsResult = null;
            }

            return true;
        }

        return false;
    }
}
