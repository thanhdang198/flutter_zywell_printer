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
