package com.leapline.bluetooth_printer;

import android.util.Log;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.printf.manager.BluetoothManager;
import com.printf.model.BluetoothModel;
import com.printf.manager.PrintfESCManager;

/** BluetoothPrinterPlugin */
public class BluetoothPrinterPlugin implements MethodCallHandler {

  private static final String TAG  = "BluetoothPrinterPlugin";
  private static final String NAMESPACE  = "bluetooth_printer";
  private final Registrar mRegistrar;
  private BluetoothManager bluetoothManager;
  private PrintfESCManager printfESCManager;

  private EventSink scanSink;
  private EventSink connectSink;
  private ArrayList<HashMap> bluetoothModels;

  private BluetoothPrinterPlugin(Registrar registrar){
    this.mRegistrar = registrar;
    this.bluetoothModels = new ArrayList<>();
    this.bluetoothManager = BluetoothManager.getInstance(registrar.activity());
    this.printfESCManager = PrintfESCManager.getInstance(registrar.activity());
    this.bluetoothManager.addConnectResultCallBack(connectResultCallBack);
    EventChannel scanChannel = new EventChannel(registrar.messenger(), NAMESPACE + "/scan");
    scanChannel.setStreamHandler(scanResultsHandler);
    EventChannel connectChannel = new EventChannel(registrar.messenger(), NAMESPACE + "/connect");
    connectChannel.setStreamHandler(connectResultsHandler);
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    Log.i(TAG, "Bluetooth Printer Plugin registration.");
    final MethodChannel channel = new MethodChannel(registrar.messenger(), NAMESPACE);
    BluetoothPrinterPlugin instance = new BluetoothPrinterPlugin(registrar);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (bluetoothManager == null) {
      this.bluetoothManager = BluetoothManager.getInstance(mRegistrar.activity());
      result.error("bluetooth_unavailable", "the device does not have bluetooth", null);
      return;
    }
    final Map<String, Object> arguments = call.arguments();
    switch (call.method) {
      case "getPlatformVersion":
      {
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      }
      case "isConnect":
      {
        result.success(bluetoothManager.isConnect());
        break;
      }
      case "beginSearch":
      {
        this.bluetoothModels = new ArrayList<>();
        scanSink.success(bluetoothModels);
        bluetoothManager.addScanBlueCallBack(scanBlueCallBack);
        int search = bluetoothManager.beginSearch();
        if(search == 2) {
          bluetoothManager.openBluetoothAdapter(mRegistrar.activity(),101);
        }
        result.success(search);
        break;
      }
      case "stopSearch":
      {
        bluetoothManager.removeScanBlueCallBack(scanBlueCallBack);
        bluetoothManager.stopSearch();
        result.success(true);
        break;
      }
      case "connectLastBluetooth":
      {
        bluetoothManager.connectLastBluetooth();
        result.success(true);
        break;
      }
      case "pairBluetooth":
      {
        String mac = (String) arguments.get("mac");
        bluetoothManager.pairBluetooth(mac);
        result.success(true);
        break;
      }
      case "printfTestPage":
      {
        bluetoothManager.printfTestPage();
        result.success(true);
        break;
      }
      case "write":
      {
        byte[] data = call.argument("data");
        int success = bluetoothManager.write(data);
        result.success(success);
        break;
      }
      case "close":
      {
        bluetoothManager.close();
        result.success(true);
        break;
      }
      case "initPrinter":
      {
        printfESCManager.initPrinter();
        result.success(true);
        break;
      }
      case "printfText":
      {
        String text = (String) arguments.get("text");
        printfESCManager.printfText(text);
        result.success(true);
        break;
      }
      case "printfBarcode":
      {
        byte barcodeType = (byte) arguments.get("barcodeType");
        int param1 = (int) arguments.get("param1");
        int param2 = (int) arguments.get("param2");
        int param3 = (int) arguments.get("param3");
        String content = (String) arguments.get("content");
        printfESCManager.printfBarcode(barcodeType,param1,param2,param3,content);
        result.success(true);
        break;
      }
      case "printfBitmapAsync":
      {
        String pathImage = (String) arguments.get("pathImage");
        int left = (int) arguments.get("left");
        Bitmap bitmap = BitmapFactory.decodeFile(pathImage);
        printfESCManager.printfBitmapAsync(bitmap,left);
        result.success(true);
        break;
      }
      case "printfTable":
      {
        @SuppressWarnings("unchecked")
        List<List<String>> dates = (List<List<String>>) arguments.get("dates");
        int effectiveWidth = (int) arguments.get("effectiveWidth");
        printfESCManager.printfTable(dates,effectiveWidth);
        result.success(true);
        break;
      }
      case "setPrinterCode":
      {
        bluetoothManager.close();
        result.success(true);
        break;
      }
      case "setPrinterBold":
      {
        printfESCManager.setPrinterBold();
        result.success(true);
        break;
      }
      case "setPrinterNoBold":
      {
        printfESCManager.setPrinterNoBold();
        result.success(true);
        break;
      }
      case "setLargeFontSize":
      {
        printfESCManager.setLargeFontSize();
        result.success(true);
        break;
      }
      case "setExtraLargeFontSize":
      {
        printfESCManager.setExtraLargeFontSize();
        result.success(true);
        break;
      }
      case "setDefaultPrinterFontSize":
      {
        printfESCManager.setDefaultPrinterFontSize();
        result.success(true);
        break;
      }
      default:
      {
        result.notImplemented();
        break;
      }
    }
  }

  private final BluetoothManager.ScanBlueCallBack scanBlueCallBack = new BluetoothManager.ScanBlueCallBack() {
    @Override
    public void scanDevice(BluetoothModel bluetoothModel) {
      Log.i(TAG, bluetoothModel.toString());
      HashMap<String, Object> deviceMap = new HashMap<>();
      deviceMap.put("name", bluetoothModel.getBluetoothName());
      deviceMap.put("mac", bluetoothModel.getBluetoothMac());
      deviceMap.put("state", bluetoothModel.getBluetoothState());
      bluetoothModels.add(deviceMap);
      for (int i = 0; i <bluetoothModels.size()-1; i++) {
        for (int j = bluetoothModels.size()-1; j >i; j--) {
          if (bluetoothModels.get(j).equals(bluetoothModels.get(i))) {
            bluetoothModels.remove(j);
          }
        }
      }
      scanSink.success(bluetoothModels);
    }
  };

  private final StreamHandler scanResultsHandler = new StreamHandler() {
    @Override
    public void onListen(Object o, EventSink eventSink) {
      scanSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
      scanSink = null;
      Log.i(TAG, "scanSink onCancel");
    }
  };

  private final BluetoothManager.ConnectResultCallBack connectResultCallBack = new BluetoothManager.ConnectResultCallBack() {
    @Override
    public void success(BluetoothDevice device) {
      Log.i(TAG, "connect success " + device.getName());
      connectSink.success(1);
    }

    @Override
    public void close(BluetoothDevice device) {
      Log.i(TAG, "connect close");
      connectSink.success(0);
    }

    @Override
    public void fail(BluetoothDevice device) {
      Log.i(TAG, "connect fail");
      connectSink.success(-1);
    }
  };

  private final StreamHandler connectResultsHandler = new StreamHandler() {
    @Override
    public void onListen(Object o, EventSink eventSink) {
      connectSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
      connectSink = null;
      Log.i(TAG, "connectSink onCancel");
    }
  };
}
