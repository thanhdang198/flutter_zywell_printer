# zywell_printer

A new Flutter plugin project for connect to zywell printer.

## Getting Started

```dart
    import 'package:zywell_printer/zywell_printer.dart';
```

## Methods
- Define the plugin in your class 
```dart
  final _zywellPrinterPlugin = ZywellPrinter();
  List<NetworkAddress> devices = [];
```
- Get the list of available devices
```dart
  Future<void> getDevices() async {
    /// 192.168.0.207 is the default ip of the printer
    /// You can change it to your local network ip, 
    /// SDK will scan the network and return the available devices
    _zywellPrinterPlugin.scanWifi((NetworkAddress addr) {
        print('Found device: ${addr.ip}');
        setState(() {
        devices.add(addr);
        });
    }, '192.168.0.207');
  }
```
- Connect to the printer
```dart
    bool isSuccess = await _zywellPrinterPlugin
        .connectIp(devices[index].ip);
```
- Print the text
```dart
    List<PrintRowData> printData = [
    PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 10),
    PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 50),
    PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 90),
    PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 130),
    PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 170), 
    PrintRowData(content: 'Hello World', paddingToTopOfInvoice: 170),
    ];
    // 50 and 80 is invoiceWidth and invoiceHeight
    await _zywellPrinterPlugin.printText(printData, 50, 80);
```