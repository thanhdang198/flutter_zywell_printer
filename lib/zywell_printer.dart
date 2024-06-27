import 'print_row_data.dart';
import 'zywell_printer_platform_interface.dart';

class ZywellPrinter {
  Future<String?> getPlatformVersion() {
    return ZywellPrinterPlatform.instance.getPlatformVersion();
  }

  Future<void> connectIp(String ip) {
    return ZywellPrinterPlatform.instance.connectIp(ip);
  }

  Future<void> disconnect() {
    return ZywellPrinterPlatform.instance.disconnect();
  }

  Future<void> printText(
      List<PrintRowData> printData, double invoiceWidth, double invoiceHeight) {
    List printDataJson = printData.map((e) => e.toJson()).toList();
    return ZywellPrinterPlatform.instance.printText({
      'printData': printDataJson,
      'invoiceWidth': invoiceWidth,
      'invoiceHeight': invoiceHeight
    });
  }

  Future<void> connectBluetooth(String address) {
    return ZywellPrinterPlatform.instance.connectBluetooth(address);
  }

  Future<void> connectUSB(String address) {
    return ZywellPrinterPlatform.instance.connectUSB(address);
  }
}
