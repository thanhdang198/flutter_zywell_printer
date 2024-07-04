# zywell_printer

A new Flutter plugin project for connect to zywell printer.

## Getting Started

```dart
    import 'package:zywell_printer/zywell_printer.dart';
```

## Methods
### Define the plugin in your class 
```dart
  final _zywellPrinterPlugin = ZywellPrinter();
  List<NetworkAddress> devices = [];
```
### Get the list of available devices
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
### Connect to the printer
```dart
    bool isSuccess = await _zywellPrinterPlugin
        .connectIp(devices[index].ip);
```
## Print the invoice by Flutter Widget (suggest use this for better quality)
Use this library to capture widget and convert it to image
```dart
    widgets_to_image: ^1.0.0
```
### Define invoice with Flutter view
```dart
  WidgetsToImage(
    controller: controller,
    child: const DemoInvoice(),
  ),
``` 
`DemoInvoice` is the widget that you want to print, demo code [here](/example/lib/demo_invoce.dart). The widget must have background color/
### Print the widget, use controller to capture the widget
```dart
  try{
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

  } catch (e) {
    print(e);
    print('Zywell Printer Plugin Error');
    print('Failed to print picture.');
  }
```
## Print the text
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