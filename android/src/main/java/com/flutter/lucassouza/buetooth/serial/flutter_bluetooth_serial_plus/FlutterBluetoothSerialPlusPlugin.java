package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class FlutterBluetoothSerialPlusPlugin implements
        FlutterPlugin,
        ActivityAware,
        MethodCallHandler {
    private MethodChannel channel;
    private FlutterBluetoothSerialPlusFunctions functions;

    private Activity activity;
    private static final String TAG = FlutterBluetoothSerialPlusPlugin.class.getSimpleName();

    @Override
    public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
        Log.d(TAG, "onAttachedToEngine");

        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bluetooth_serial_plus");
        channel.setMethodCallHandler(this);

        functions = new FlutterBluetoothSerialPlusFunctions();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("scanDevices")) {
            result.success(functions.scanDevices());
        } else if (call.method.equals("connect")) {
            result.success(functions.connect(call.arguments()));
        } else if (call.method.equals("disconnect")) {
            result.success(functions.disconnect());
        } else if (call.method.equals("write")) {
            result.success(functions.write(call.arguments()));
        } else if (call.method.equals("checkPermissions")) {
            result.success(functions.checkPermissions(activity));
        } else if (call.method.equals("requestPermissions")) {
            result.success(functions.requestPermissions(activity));
        } else if (call.method.equals("bluetoothEnabled")) {
            result.success(functions.bluetoothEnabled());
        } else if (call.method.equals("enableBluetooth")) {
            functions.enableBluetooth(activity, enabled -> result.success(enabled));
        } else if (call.method.equals("connectedDevice")) {
            result.success(functions.connectedDevice());
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
        Log.d(TAG, "onAttachedToActivity");

        activity = activityPluginBinding.getActivity();

        BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

              if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                if (state == BluetoothAdapter.STATE_OFF) {
                  functions.disconnect();

                  channel.invokeMethod("onStateChange",
                          FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(2, null)
                  );
                } else if (state == BluetoothAdapter.STATE_ON) {
                  channel.invokeMethod("onStateChange",
                          FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(3, null)
                  );
                }
              } else {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                  Log.d(TAG, "Device found");
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                  Log.d(TAG, "Device connected");
                  channel.invokeMethod("onStateChange",
                          FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(0, device)
                  );
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                  Log.d(TAG, "Done searching");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                  Log.d(TAG, "Device is about to disconnect");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                  Log.d(TAG, "Device has disconnect");
                  functions.disconnect();

                  channel.invokeMethod("onStateChange",
                          FlutterBluetoothSerialPlusMapUtils.bluetoothStateToMap(1, device)
                  );
                }
              }
            }
        };

      activity.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
      activity.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
      activity.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
      activity.registerReceiver(bluetoothDeviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}
