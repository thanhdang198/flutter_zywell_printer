import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'zywell_printer_platform_interface.dart';

/// An implementation of [ZywellPrinterPlatform] that uses method channels.
class MethodChannelZywellPrinter extends ZywellPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('zywell_printer');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> connectIp(String ip) async {
    return await methodChannel.invokeMethod('connectIp', ip);
  }

  @override
  Future<void> disconnect() async {
    await methodChannel.invokeMethod('disconnect');
  }

  @override
  Future<void> printImage(dynamic data) async {
    await methodChannel.invokeMethod('printImage', data);
  }

  @override
  Future<void> printText(dynamic text) async {
    await methodChannel.invokeMethod('printText', text);
  }

  @override
  Future<void> connectBluetooth(String address) async {
    await methodChannel.invokeMethod('connectBluetooth', address);
  }

  @override
  Future<void> connectUSB(String address) async {
    await methodChannel.invokeMethod('connectUSB', address);
  }
}
