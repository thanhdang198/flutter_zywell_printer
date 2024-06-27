import Flutter
import UIKit

public class ZywellPrinterPlugin: NSObject, FlutterPlugin {
//    public func poswifiManager(_ manager: POSWIFIManager!, didConnectedToHost host: String!, port: UInt16) {
//        
//    }
//    
//    public func poswifiManager(_ manager: POSWIFIManager!, willDisconnectWithError error: Error!) {
//        
//    }
//    
//    public func poswifiManager(_ manager: POSWIFIManager!, didWriteDataWithTag tag: Int) {
//        
//    }
//    
//    public func poswifiManager(_ manager: POSWIFIManager!, didRead data: Data!, tag: Int) {
//        
//    }
//    
//    public func poswifiManagerDidDisconnected(_ manager: POSWIFIManager!) {
//        
//    }
    
    
    var wifiManager = POSWIFIManager.share();
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "zywell_printer", binaryMessenger: registrar.messenger())
    let instance = ZywellPrinterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "connectIp":
        let args = call.arguments as! String
        connectIp(ipAddress: args)
    case "printText":
        let data = call.arguments as? NSDictionary?
            //guard let oWayPoints = arguments?["wayPoints"] as? NSDictionary else {return}
            
        guard let invoiceWidth = data?!["invoiceWidth"] as? Double
                else{return}
        guard let invoiceHeight = data?!["invoiceHeight"] as? Double else {
                return
            }
        guard let printData = data?!["printData"] as? [NSDictionary] else {
                return
            }

                printText(printData: printData, invoiceWidth: invoiceWidth, invoiceHeight: invoiceHeight)
            
        
    default:
      result(FlutterMethodNotImplemented)
    }
  }
    @objc func connectIp(ipAddress: String) -> Void {
        
        wifiManager?.posDisConnect();
        wifiManager?.posConnect(withHost: ipAddress, port: 9100, completion: { (isConnect) in
            if isConnect {
                print("connected")
            } else {
                print("not connected")
            }
        })
        
    }
    @objc func printText(printData: [NSDictionary], invoiceWidth: Double, invoiceHeight: Double) -> Void {
        let dataM = NSMutableData()
        var data = NSData()

        // data = self.codeTextField.text?.data(using: .ascii) as NSData?
        data = TscCommand.sizeBymm(withWidth: invoiceWidth, andHeight: invoiceHeight) as NSData
        dataM.append(data as Data)

        data = TscCommand.gapBymm(withWidth: 3, andHeight: 0) as NSData
        dataM.append(data as Data)

        data = TscCommand.cls() as NSData
        dataM.append(data as Data)
        
        for item in printData {
            let x = item["paddingLeft"] as! Int32
            let y = item["paddingToTopOfInvoice"] as! Int32
            let font = item["font"] as! String
            let content = item["text"] as! String
            data = TscCommand.textWith(x: x, andY: y, andFont: font, andRotation: 0, andX_mul: 1, andY_mul: 1, andContent: content, usStrEnCoding: NSASCIIStringEncoding) as NSData
            dataM.append(data as Data)
        }
//        data = TscCommand.textWith(x: 0, andY: 0, andFont: "TSS24.BF2", andRotation: 0, andX_mul: 1, andY_mul: 1, andContent: "12345678abcd", usStrEnCoding: NSASCIIStringEncoding) as NSData
//        dataM.append(data as Data)

        data = TscCommand.print(1) as NSData
        dataM.append(data as Data)

//        if blueToothModel.isOn == false {
            wifiManager?.posWriteCommand(with: dataM as Data)
        
//        } else {
//            manager.POSWriteCommand(with: dataM as Data)
//        }

    }

}
