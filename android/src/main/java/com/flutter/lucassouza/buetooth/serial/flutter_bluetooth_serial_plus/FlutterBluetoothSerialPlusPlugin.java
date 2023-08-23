package com.flutter.lucassouza.buetooth.serial.flutter_bluetooth_serial_plus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FlutterBluetoothSerialPlusPlugin */
public class FlutterBluetoothSerialPlusPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private FlutterBluetoothSerialPlusFunctions functions;

  @Override
  public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bluetooth_serial_plus");
    channel.setMethodCallHandler(this);

    functions = new FlutterBluetoothSerialPlusFunctions();
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("scanDevices")) {
      result.success(functions.scanDevices());
    } else if(call.method.equals("connect")) {
      result.success(functions.connect(call.arguments()));
    } else if(call.method.equals("disconnect")){
      result.success(functions.disconnect());
    } else if(call.method.equals("write")){
      result.success(functions.write(call.arguments()));
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
