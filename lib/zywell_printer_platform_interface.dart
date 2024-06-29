import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'zywell_printer_method_channel.dart';

abstract class ZywellPrinterPlatform extends PlatformInterface {
  /// Constructs a ZywellPrinterPlatform.
  ZywellPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static ZywellPrinterPlatform _instance = MethodChannelZywellPrinter();

  /// The default instance of [ZywellPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelZywellPrinter].
  static ZywellPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ZywellPrinterPlatform] when
  /// they register themselves.
  static set instance(ZywellPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> connectIp(String ip) {
    throw UnimplementedError('connectIp() has not been implemented.');
  }

  // disconnect
  Future<void> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  Future<void> printText(dynamic text) {
    throw UnimplementedError('printText() has not been implemented.');
  }

  Future<void> connectBluetooth(String address) {
    throw UnimplementedError('connectBluetooth() has not been implemented.');
  }

  Future<void> connectUSB(String address) {
    throw UnimplementedError('connectUSB() has not been implemented.');
  }
}
