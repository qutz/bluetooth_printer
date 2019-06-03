import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bluetooth_printer/bluetooth_printer.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List devices = [];
  int connect = -1;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  @override
  void dispose() {
    super.dispose();
    BluetoothPrinter.close();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    BluetoothPrinter.listenScan(_onScan, _onPrinterError);
    BluetoothPrinter.listenConnect(_onConnect, _onPrinterError);
    BluetoothPrinter.connectLastBluetooth();

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  void _onScan(Object event) {
    setState(() {
      devices = event;
    });
    print(devices);
  }

  void _onConnect(Object event) {
    if (event == 1) {
    } else if (event == 0) {
    } else if (event == -1) {}
    setState(() {
      connect = event;
    });
    print(event);
  }

  void _onPrinterError(Object event) {
    print(event);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('连接状态: ${connect == 1 ? '连接成功': '连接失败'}'),
            RaisedButton(
              onPressed: () async {
                var result = await BluetoothPrinter.beginSearch();
                print(result);
                print(await BluetoothPrinter.connectLastBluetooth());
              },
              child: Text('连接上次设备'),
            ),
            RaisedButton(
              onPressed: () async {
                print(await BluetoothPrinter.isConnect());
              },
              child: Text('测试连接'),
            ),
            RaisedButton(
              onPressed: () async {
                print(await BluetoothPrinter.beginSearch());
              },
              child: Text('开始搜索'),
            ),
            RaisedButton(
              onPressed: () async {
                print(await BluetoothPrinter.stopSearch());
              },
              child: Text('停止搜索'),
            ),
            Container(
              height: 300,
              child: devices.length > 0
                  ? ListView.builder(
                      itemCount: devices.length,
                      itemBuilder: (context, index) {
                        return new ListTile(
                          onTap: () async {
                            await BluetoothPrinter.pairBluetooth(
                                mac: devices[index]["mac"]);
                          },
                          leading: new Icon(Icons.usb),
                          title: new Text(devices[index]["name"] +
                              " " +
                              devices[index]["mac"]),
                          subtitle: new Text(
                              devices[index]["state"] == 1 ? '已配对' : '未配对'),
                        );
                      },
                    )
                  : Text('蓝牙设备列表'),
            )
          ],
        ),
      ),
    );
  }
}
