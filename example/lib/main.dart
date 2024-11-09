import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:widgets_to_image/widgets_to_image.dart';
import 'package:zywell_printer/print_row_data.dart';
import 'package:zywell_printer/zywell_printer.dart';

import 'package:zywell_printer_example/demo_invoce.dart';

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
  WidgetsToImageController controller = WidgetsToImageController();
// to save image bytes of widget
  Uint8List? bytes;

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

  clearBuffer() async {
    try {
      await _zywellPrinterPlugin.clearBuffer();
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to clear buffer.');
    }
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

  printPicture() async {
    try {
      /// Load bytes from assets
      ByteData imageBytes = await rootBundle.load('assets/80m.png');

      /// Convert to Uint8List
      List<int> values = imageBytes.buffer.asUint8List();

      Uint8List bytes = Uint8List.fromList(values);

      _zywellPrinterPlugin.printThermalImage(
          image: bytes,

          /// Độ rộng của hoá đơn
          invoiceWidth: 80,

          /// Độ dài của hoá đơn
          invoiceHeight: 200,

          /// Padding trái phải của ảnh
          gapWidth: 50,

          /// Padding trên dưới của ảnh
          gapHeight: 10,

          /// Độ rộng của ảnh dùng để in
          imageTargetWidth: 600);
//192.168.0.207
      // _zywellPrinterPlugin.printImage(
      //     image: bytes,
      //     invoiceWidth: 50,
      //     invoiceHeight: 200,
      //     gapWidth: 10,
      //     gapHeight: 10,
      //     imageTargetWidth: 450);
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to print picture.');
    }
  }

  printWidget() async {
    try {
      final d = await controller.capture();
      setState(() {
        bytes = d;
      });
      _zywellPrinterPlugin.printImage(
          image: d!,

          /// Độ rộng của hoá đơn
          invoiceWidth: 80,

          /// Độ dài của hoá đơn
          invoiceHeight: 160,

          /// Padding trái phải của ảnh
          gapWidth: 10,

          /// Padding trên dưới của ảnh
          gapHeight: 10,

          /// Độ rộng của ảnh dùng để in
          imageTargetWidth: 600);

      // _zywellPrinterPlugin.printImage(
      //     image: bytes,
      //     invoiceWidth: 50,
      //     invoiceHeight: 200,
      //     gapWidth: 10,
      //     gapHeight: 10,
      //     imageTargetWidth: 450);
    } catch (e) {
      print(e);
      print('Zywell Printer Plugin Error');
      print('Failed to print picture.');
    }
  }

  printText() async {
    try {
      List<PrintRowData> printData = [
        PrintRowData(
            content: 'Hoa don ban hang', paddingToTopOfInvoice: 10, font: '3'),
        PrintRowData(
            content: 'TrueMotorCare', paddingToTopOfInvoice: 50, font: '4'),
        PrintRowData(
            content: 'Motor Care', paddingToTopOfInvoice: 90, font: '5'),
        PrintRowData(
            content: '-------------------------',
            paddingToTopOfInvoice: 150,
            font: '6'),
        PrintRowData(
            content: 'Don gia      So luong       Thanh tien',
            paddingToTopOfInvoice: 170,
            font: '7'),
        PrintRowData(
            content: 'Chinh ga         1           100.000đ',
            paddingToTopOfInvoice: 200,
            font: '8'),
      ];
      await _zywellPrinterPlugin.printText(
          printData: printData, invoiceWidth: 50, invoiceHeight: 80);
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
                }, '192.168.1.207');
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
                  printPicture();
                },
                child: const Icon(Icons.image)),
            FloatingActionButton(
                onPressed: () {
                  printWidget();
                },
                child: const Icon(Icons.widgets)),
            FloatingActionButton(
              onPressed: () {
                connectPrinter();
              },
              tooltip: 'Increment',
              child: const Icon(Icons.connect_without_contact),
            ),
            FloatingActionButton(
              onPressed: () {
                // connectIp();
                clearBuffer();
              },
              tooltip: 'Increment',
              child: const Icon(Icons.clear),
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
                      )),
              WidgetsToImage(
                controller: controller,
                child: const DemoInvoice(),
              ),
              if (bytes != null) Image.memory(bytes!),
            ],
          ),
        ),
      ),
    );
  }
}
