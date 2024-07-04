import 'dart:convert';
import 'dart:developer';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:ping_discover_network_plus/ping_discover_network_plus.dart';
export 'package:ping_discover_network_plus/ping_discover_network_plus.dart';

import 'print_row_data.dart';
import 'zywell_printer_platform_interface.dart';

class ZywellPrinter {
  /// Change ip to your printer ip/ local network ip.
  /// The SDK will scan the network for the printer.
  Future<void> scanWifi(Function(NetworkAddress addr) onDeviceFound,
      [String ip = '192.168.1.1', int port = 9100]) async {
    final String subnet = ip.substring(0, ip.lastIndexOf('.'));
    try {
      final stream = NetworkAnalyzer.i.discover2(subnet, port);

      stream.listen((NetworkAddress addr) {
        if (addr.exists) {
          onDeviceFound(addr);
        }
      })
        ..onDone(() {})
        ..onError((dynamic e) {});
    } catch (e) {
      log(e.toString());
    }
  }

  Future<String?> getPlatformVersion() {
    return ZywellPrinterPlatform.instance.getPlatformVersion();
  }

  Future<bool> connectIp(String ip) {
    return ZywellPrinterPlatform.instance.connectIp(ip);
  }

  Future<void> disconnect() {
    return ZywellPrinterPlatform.instance.disconnect();
  }

  Future<void> printImage(
      {required Uint8List image,
      double invoiceWidth = 50,
      required double invoiceHeight,
      double gapWidth = 0,
      double gapHeight = 0,
      double imageTargetWidth = 0}) {
    return ZywellPrinterPlatform.instance.printImage({
      'image': base64.encode(image),
      'invoiceWidth': invoiceWidth,
      'invoiceHeight': invoiceHeight,
      'gapWidth': gapWidth,
      'gapHeight': gapHeight,
      'imageTargetWidth': imageTargetWidth
    });
  }

  Future<void> printText(
      {required List<PrintRowData> printData,
      double invoiceWidth = 50,
      required double invoiceHeight}) {
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
