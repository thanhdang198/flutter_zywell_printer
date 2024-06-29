import 'package:flutter_test/flutter_test.dart';
import 'package:zywell_printer/zywell_printer.dart';
import 'package:zywell_printer/zywell_printer_platform_interface.dart';
import 'package:zywell_printer/zywell_printer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockZywellPrinterPlatform
    with MockPlatformInterfaceMixin
    implements ZywellPrinterPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<void> connectBluetooth(String address) {
    // TODO: implement connectBluetooth
    throw UnimplementedError();
  }

  @override
  Future<bool> connectIp(String ip) {
    // TODO: implement connectIp
    throw UnimplementedError();
  }

  @override
  Future<void> connectUSB(String address) {
    // TODO: implement connectUSB
    throw UnimplementedError();
  }

  @override
  Future<void> disconnect() {
    // TODO: implement disconnect
    throw UnimplementedError();
  }

  @override
  Future<void> printText(text) {
    // TODO: implement printText
    throw UnimplementedError();
  }
}

void main() {
  final ZywellPrinterPlatform initialPlatform = ZywellPrinterPlatform.instance;

  test('$MethodChannelZywellPrinter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelZywellPrinter>());
  });

  test('getPlatformVersion', () async {
    ZywellPrinter zywellPrinterPlugin = ZywellPrinter();
    MockZywellPrinterPlatform fakePlatform = MockZywellPrinterPlatform();
    ZywellPrinterPlatform.instance = fakePlatform;

    expect(await zywellPrinterPlugin.getPlatformVersion(), '42');
  });
}
