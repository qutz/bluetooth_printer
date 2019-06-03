import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';

typedef void EventHandler(Object event);

class BluetoothPrinter {
  static const String namespace = 'bluetooth_printer';
  static const MethodChannel _channel = const MethodChannel(namespace);

  static const EventChannel _scanChannel =
      const EventChannel('$namespace/scan');

  static const EventChannel _connectChannel =
      const EventChannel('$namespace/connect');

  static listenScan(EventHandler onEvent, EventHandler onError) {
    _scanChannel.receiveBroadcastStream().listen(onEvent, onError: onError);
  }

  static listenConnect(EventHandler onEvent, EventHandler onError) {
    _connectChannel.receiveBroadcastStream().listen(onEvent, onError: onError);
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> isConnect() async =>
      await _channel.invokeMethod('isConnect');

  static Future<int> beginSearch() async =>
      await _channel.invokeMethod('beginSearch');

  static Future<bool> stopSearch() async =>
      await _channel.invokeMethod('stopSearch');

  static Future<bool> connectLastBluetooth() async =>
      await _channel.invokeMethod('connectLastBluetooth');

  static Future<bool> pairBluetooth({@required String mac}) async {
    final Map<String, dynamic> params = <String, dynamic>{'mac': mac};
    return await _channel.invokeMethod('pairBluetooth', params);
  }

  static Future<bool> printfTestPage() async =>
      await _channel.invokeMethod('printfTestPage');

  static Future<int> write({@required Uint8List data}) async {
    final Map<String, dynamic> params = <String, dynamic>{'data': data};
    return await _channel.invokeMethod('write', params);
  }

  static Future<bool> close() async => await _channel.invokeMethod('close');

  static Future<bool> initPrinter() async =>
      await _channel.invokeMethod('initPrinter');

  static Future<bool> printfText({@required String text}) async {
    final Map<String, dynamic> params = <String, dynamic>{'text': text};
    return await _channel.invokeMethod('printfText', params);
  }

  static Future<bool> printfBarcode(
      {@required int barcodeType,
      @required int param1,
      @required int param2,
      @required int param3,
      @required String content}) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'barcodeType': barcodeType,
      'param1': param1,
      'param2': param2,
      'param3': param3,
      'content': content
    };
    return await _channel.invokeMethod('printfBarcode', params);
  }

  static Future<bool> printfBitmapAsync(
      {@required String pathImage, @required int left}) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'pathImage': pathImage,
      'left': left
    };
    return await _channel.invokeMethod('printfBitmapAsync', params);
  }

  static Future<bool> printfTable(
      {@required List<List<String>> dates, @required int effectiveWidth}) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'dates': dates,
      'effectiveWidth': effectiveWidth
    };
    return await _channel.invokeMethod('printfTable', params);
  }

  static Future<bool> setPrinterCode() async =>
      await _channel.invokeMethod('setPrinterCode');

  static Future<bool> setPrinterBold() async =>
      await _channel.invokeMethod('setPrinterBold');

  static Future<bool> setPrinterNoBold() async =>
      await _channel.invokeMethod('setPrinterNoBold');

  static Future<bool> setLargeFontSize() async =>
      await _channel.invokeMethod('setLargeFontSize');

  static Future<bool> setExtraLargeFontSize() async =>
      await _channel.invokeMethod('setExtraLargeFontSize');

  static Future<bool> setDefaultPrinterFontSize() async =>
      await _channel.invokeMethod('setDefaultPrinterFontSize');
}

class BarcodeType {
  final int UPC_A = 0;
  final int UPC_E = 1;
  final int JAN13 = 2;
  final int JAN8 = 3;
  final int CODE39 = 4;
  final int ITF = 5;
  final int CODABAR = 6;
  final int CODE93 = 72;
  final int CODE128 = 07;
  final int PDF417 = 100;
  final int DATAMATRIX = 101;
  final int QRCODE = 102;
}
