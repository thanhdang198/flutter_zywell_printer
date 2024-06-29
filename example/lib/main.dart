import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:zywell_printer/print_row_data.dart';
import 'package:zywell_printer/zywell_printer.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _zywellPrinterPlugin = ZywellPrinter();
  List<NetworkAddress> devices = [];
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _zywellPrinterPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  connectPrinter() async {
    try {
      String platformVersion =
          await _zywellPrinterPlugin.getPlatformVersion() ??
              'Unknown platform version';

      print('Zywell Printer Plugin Success');
      print('Running on: $platformVersion\n');
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to get platform version.');
    }
  }

  connectIp() async {
    try {
      await _zywellPrinterPlugin.connectIp('192.168.0.207');
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to connect to printer.');
    }
  }

  disconnect() async {
    try {
      await _zywellPrinterPlugin.disconnect();
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to disconnect from printer.');
    }
  }

  printText() async {
    try {
      List<PrintRowData> printData = [
        PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 10),
        PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 50),
        PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 90),
        PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 130),
        PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 170),
        // PrintRowData(
        //     contentType: PrintType.barcode,
        //     content: 'Hello World',
        //     paddingToTopOfInvoice: 170),
        // PrintRowData(
        //     contentType: PrintType.qr,
        //     content: 'Hello World',
        //     paddingToTopOfInvoice: 170),
        PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 170),
      ];
      await _zywellPrinterPlugin.printText(printData, 50, 80);
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to print text.');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        floatingActionButton: Column(
          mainAxisAlignment: MainAxisAlignment.end,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            FloatingActionButton(
              onPressed: () {
                _zywellPrinterPlugin.scanWifi((NetworkAddress addr) {
                  print('Found device: ${addr.ip}');
                  setState(() {
                    devices.add(addr);
                  });
                }, '192.168.0.207');
              },
              tooltip: 'Scan',
              child: const Icon(Icons.scanner),
            ),
            FloatingActionButton(
              onPressed: () {
                printText();
              },
              tooltip: 'Increment',
              child: const Icon(Icons.print),
            ),
            FloatingActionButton(
              onPressed: () {
                connectPrinter();
              },
              tooltip: 'Increment',
              child: const Icon(Icons.connect_without_contact),
            ),
            FloatingActionButton(
              onPressed: () {
                connectIp();
              },
              tooltip: 'Increment',
              child: const Icon(Icons.add),
            ),
            FloatingActionButton(
              onPressed: () {
                disconnect();
              },
              tooltip: 'Increment',
              child: const Icon(Icons.remove),
            ),
          ],
        ),
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              ...List.generate(
                  devices.length,
                  (index) => InkWell(
                        onTap: () async {
                          bool isSuccess = await _zywellPrinterPlugin
                              .connectIp(devices[index].ip);
                          if (isSuccess) {
                            setState(() {
                              _platformVersion =
                                  'Connected to ${devices[index].ip}';
                            });
                            print('Connected to ${devices[index].ip}');
                          } else {
                            setState(() {
                              _platformVersion =
                                  'Failed to connect to ${devices[index].ip}';
                            });
                            print('Failed to connect to ${devices[index].ip}');
                          }
                        },
                        child: Container(
                          margin: const EdgeInsets.all(10),
                          decoration: BoxDecoration(
                              border: Border.all(color: Colors.black),
                              borderRadius: BorderRadius.circular(10)),
                          child: ListTile(
                            title: Text(devices[index].ip),
                          ),
                        ),
                      ))
            ],
          ),
        ),
      ),
    );
  }
}
