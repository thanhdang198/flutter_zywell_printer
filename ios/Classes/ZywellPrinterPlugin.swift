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
    var bleManager = BLEManager.shared();
    
    var connectedPeripherals = [CBPeripheral]()
    var wifiManagerDictionary = [String: POSWIFIManager]()
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "zywell_printer", binaryMessenger: registrar.messenger())
//      bleManager?.delegate = self
    let instance = ZywellPrinterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
        
        var res = TscCommand.checkPrinterStatusByPort9100();
        print(res ?? "Failed to check")
      result("iOS " + UIDevice.current.systemVersion)
    case "connectIp":
        let args = call.arguments as! String
        connectIp(ipAddress: args, result: result)
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
            
    case "disconnect":
        wifiManager?.posDisConnect()
        
    default:
      result(FlutterMethodNotImplemented)
    }
  }
    // MARK: - connect Bluetooth
    @objc(connectBLE:result:)
    func connectBLE(address: String, result: @escaping FlutterResult) {
        bleManager?.stopScan()
        var connectedPeripheral: CBPeripheral? = nil
        for peripheral in connectedPeripherals {
            if peripheral.identifier.uuidString == address {
                connectedPeripheral = peripheral
                break
            }
        }
        if let peripheral = connectedPeripheral {
            bleManager?.connectPeripheral(peripheral)
            result(nil)
        } else {
            // MARK: - TODO: Handle scan bluetooth here
//            bleManager?.startScan { discoveredPeripheral, address in
//                if address == address {
//                    self.bleManager?.connectPeripheral(discoveredPeripheral)
//                    resolve(nil)
//                }
//            }
        }
    }
    
    // MARK: - Connect wifi
    @objc func connectIp(ipAddress: String, result: @escaping FlutterResult) -> Void {
        // End previous session and start print new page
        
        wifiManager?.posDisConnect();
        wifiManager?.posConnect(withHost: ipAddress, port: 9100, completion: { (isConnect) in
            if isConnect {
                result(true)
                print("connected")
            } else {
                result(false)
                print("not connected")
            }
        })
        
    }
    // MARK: -
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

        data = TscCommand.eoj() as NSData
        dataM.append(data as Data)

        
        data = TscCommand.delay(3000 ) as NSData
        dataM.append(data as Data)
        
        data = TscCommand.print(1) as NSData
        dataM.append(data as Data)

//        if blueToothModel.isOn == false {
            wifiManager?.posWriteCommand(with: dataM as Data)
        
//        } else {
//            manager.POSWriteCommand(with: dataM as Data)
//        }

    }

    
//    @objc(printPic:result:)
//    func printPic(address: String, result: @escaping FlutterResult) {
//        do {
//            guard let wifiManager = wifiManagerDictionary[address] else {
//                let error = NSError(domain: "ZywellModuleErrorDomain", code: 1002, userInfo: [NSLocalizedDescriptionKey: "Printer is not connected"])
//                result("printer_not_connected")
//                return
//            }
//            
//            let mode = options["mode"] as? String ?? ""
//            let labelString = "LABEL"
//            
//            if mode == labelString {
//                print("mode is equal to LABEL")
//                let isDisconnect = options["is_disconnect"] as? Bool ?? false
//                let isResolve = options["is_resolve"] as? Bool ?? false
//                let nWidth = options["width"] as? Int ?? 0
//                var paperSize = options["paper_size"] as? Int ?? 50
//                
//                let width = ((nWidth + 7) / 8) * 8
//                guard let newImage = imageCompressForWidthScale(imagePath: imagePath, targetWidth: CGFloat(width)) else { return }
//                
//                var dataM = Data()
//                var data = Data()
//                data = TscCommand.sizeBymm(width: paperSize, height: 30)
//                dataM.append(data)
//                data = TscCommand.gapBymm(width: 3, height: 0)
//                dataM.append(data)
//                data = TscCommand.cls()
//                dataM.append(data)
//                data = TscCommand.bitmap(x: -2, y: 10, mode: 0, image: newImage, bmpType: .dithering)
//                dataM.append(data)
//                data = TscCommand.print(count: 1)
//                dataM.append(data)
//                
//                wifiManager.POSWriteDataWithCallback(dataM, completion: { success in
//                    if success && isDisconnect {
//                        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
//                            wifiManager.POSDisConnect()
//                        }
//                    }
//                    if success && isResolve {
//                        resolve(true)
//                    }
//                })
//            } else {
//                print("mode is NOT equal to LABEL")
//                let nWidth = options["width"] as? Int ?? 0
//                let imageURL = URL(fileURLWithPath: imagePath)
//                let inputImage = CIImage(contentsOf: imageURL)
//                
//                let filter = CIFilter(name: "CIColorControls")!
//                filter.setValue(inputImage, forKey: kCIInputImageKey)
//                filter.setValue(0.0, forKey: kCIInputSaturationKey)
//                
//                let isDisconnect = options["is_disconnect"] as? Bool ?? false
//                let isResolve = options["is_resolve"] as? Bool ?? false
//                
//                guard let outputImage = filter.outputImage,
//                      let context = CIContext() as CIContext?,
//                      let outputCGImage = context.createCGImage(outputImage, from: outputImage.extent) else { return }
//                
//                let newImage = UIImage(cgImage: outputCGImage)
//                let imgHeight = newImage.size.height
//                let imagWidth = newImage.size.width
//                let width = ((nWidth + 7) / 8) * 8
//                let size = CGSize(width: width, height: imgHeight * width / imagWidth)
//                
//                guard let scaled = ImageTranster.imgWithImage(newImage, scaledToFillSize: size) else { return }
//                guard let graImage = ImageTranster.imgToGreyImage(scaled) else { return }
//                guard let formatedData = ImageTranster.img_format_K_threshold(graImage, width: Int(size.width), height: Int(size.height)) else { return }
//                let dataToPrint = ImageTranster.convertEachLinePixToCmd(formatedData, nWidth: Int(size.width), nHeight: Int(size.height), nMode: 0)
//                
//                wifiManager.POSWriteCommandWithData(dataToPrint)
//                wifiManager.POSWriteCommandWithData(PosCommand.printAndFeedLine())
//                wifiManager.POSWriteCommandWithData(PosCommand.printAndFeedLine())
//                wifiManager.POSWriteCommandWithData(PosCommand.printAndFeedLine())
//                wifiManager.POSWriteCommandWithData(PosCommand.printAndFeedLine())
//                wifiManager.POSWriteDataWithCallback(PosCommand.selectCutPageModelAndCutpage(0), completion: { success in
//                    if success && isDisconnect {
//                        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
//                            wifiManager.POSDisConnect()
//                        }
//                    }
//                    if success && isResolve {
//                        resolve(true)
//                    }
//                })
//            }
//        } catch {
//            let error = NSError(domain: "RCTZywellThermalPrinterErrorDomain", code: 1002, userInfo: [NSLocalizedDescriptionKey: "Failed to print net"])
//            reject("failed_to_print_net", "Failed to print net", error)
//            print("ERROR IN PRINTING IMG: \(error.localizedDescription)")
//        }
//    }
    
    
    @objc(printPic:imagePath:printerOptions:result:)
    func printPic(ipAddress: String, imagePath: String, options: [String: Any], result: @escaping FlutterResult) {
        do {
            guard let wifiManager = wifiManagerDictionary[ipAddress] else {
                let error = NSError(domain: "ZywellModuleErrorDomain", code: 1002, userInfo: [NSLocalizedDescriptionKey: "Printer is not connected"])
                result("printer_not_connected")
                return
            }
            
            let mode = options["mode"] as? String ?? ""
            let labelString = "LABEL"
            
            if mode == labelString {
                print("mode is equal to LABEL")
                let isDisconnect = options["is_disconnect"] as? Bool ?? false
                let isResolve = options["is_resolve"] as? Bool ?? false
                let nWidth = options["width"] as? Int ?? 0
                var paperSize = options["paper_size"] as? Int ?? 50
                
                let width = ((nWidth + 7) / 8) * 8
                guard let newImage = imageCompressForWidthScale(imagePath: imagePath, targetWidth: CGFloat(width)) else { return }
                
                var dataM = Data()
                var data = Data()
                data = TscCommand.sizeBymm(withWidth: Double(paperSize), andHeight: 30)
                dataM.append(data)
                data = TscCommand.gapBymm(withWidth: 3, andHeight: 0)
                dataM.append(data)
                data = TscCommand.cls()
                dataM.append(data)
                data = TscCommand.bitmapWith(x: -2, andY:  10, andMode: 0, andImage: newImage, andBmpType: Dithering)
                dataM.append(data)
                data = TscCommand.print(1)
                dataM.append(data)
                
                wifiManager.posWriteCommand(with: dataM, withResponse: { success in
                    if (success != nil) && isDisconnect {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                            wifiManager.posDisConnect()
                        }
                    }
                    if (success != nil) && isResolve {
                        result(true)
                    }
                })
            } else {
                print("mode is NOT equal to LABEL")
                let nWidth = options["width"] as? Int ?? 0
                let imageURL = URL(fileURLWithPath: imagePath)
                let inputImage = CIImage(contentsOf: imageURL)
                
                let filter = CIFilter(name: "CIColorControls")!
                filter.setValue(inputImage, forKey: kCIInputImageKey)
                filter.setValue(0.0, forKey: kCIInputSaturationKey)
                
                let isDisconnect = options["is_disconnect"] as? Bool ?? false
                let isResolve = options["is_resolve"] as? Bool ?? false
                
                guard let outputImage = filter.outputImage,
                      let context = CIContext() as CIContext?,
                      let outputCGImage = context.createCGImage(outputImage, from: outputImage.extent) else { return }
                
                let newImage = UIImage(cgImage: outputCGImage)
                let imgHeight = newImage.size.height
                let imagWidth = newImage.size.width
                let width = ((nWidth + 7) / 8) * 8
                let size = CGSize(width: width, height: Int(imgHeight) * width / Int(imagWidth))
                
                guard let scaled = ImageTranster.img(with:newImage, scaledToFill: size) else { return }
                guard let graImage = ImageTranster.img(toGreyImage: scaled) else { return }
                guard let formatedData = ImageTranster.img_format_K_threshold(graImage, width: Int(size.width), height: Int(size.height)) else { return }
                let dataToPrint = ImageTranster.convertEachLinePix(toCmd:formatedData, nWidth: Int(size.width), nHeight: Int(size.height), nMode: 0)
                
                wifiManager.posWriteCommand(with: dataToPrint)
                wifiManager.posWriteCommand(with: PosCommand.printAndFeedLine())
                wifiManager.posWriteCommand(with: PosCommand.printAndFeedLine())
                wifiManager.posWriteCommand(with: PosCommand.printAndFeedLine())
                wifiManager.posWriteCommand(with: PosCommand.printAndFeedLine())
                wifiManager.posWriteCommand(with: PosCommand.selectCutPageModelAndCutpage(0), withResponse:  { success in
                    if (success != nil) && isDisconnect {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                            wifiManager.posDisConnect()
                        }
                    }
                    if (success != nil) && isResolve {
                        result(true)
                    }
                })
            }
        } catch {
            let error = NSError(domain: "RCTZywellThermalPrinterErrorDomain", code: 1002, userInfo: [NSLocalizedDescriptionKey: "Failed to print net"])
            result("failed_to_print_net")
            print("ERROR IN PRINTING IMG: \(error.localizedDescription)")
        }
    }
    
    func convertToGrayScaleWithBlackAndWhite(sourceImage: UIImage?) -> UIImage? {
        guard let sourceImage = sourceImage else {
            print("Source image is nil")
            return nil
        }
        
        let size = sourceImage.size
        let rect = CGRect(origin: .zero, size: size)
        
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        guard UIGraphicsGetCurrentContext() != nil else { return nil }
        
        sourceImage.draw(in: rect, blendMode: .luminosity, alpha: 1.0)
        guard let grayImage = UIGraphicsGetImageFromCurrentImageContext() else { return nil }
        
        UIGraphicsEndImageContext()
        
        let colorSpace = CGColorSpaceCreateDeviceGray()
        guard let bitmapContext = CGContext(data: nil, width: Int(grayImage.size.width), height: Int(grayImage.size.height), bitsPerComponent: 8, bytesPerRow: Int(grayImage.size.width), space: colorSpace, bitmapInfo: CGImageAlphaInfo.none.rawValue) else { return nil }
        
        bitmapContext.draw(grayImage.cgImage!, in: CGRect(origin: .zero, size: grayImage.size))
        guard let bwImageRef = bitmapContext.makeImage() else { return nil }
        
        let bwImage = UIImage(cgImage: bwImageRef)
        return bwImage
    }
    
    func imageCompressForWidthScale(imagePath: String, targetWidth: CGFloat) -> UIImage? {
        guard let image = UIImage(contentsOfFile: imagePath),
              let sourceImage = convertToGrayScaleWithBlackAndWhite(sourceImage: image) else {
            print("Failed to load image from path: \(imagePath)")
            return nil
        }
        
        let imageSize = sourceImage.size
        let width = imageSize.width
        let height = imageSize.height
        let targetHeight = height / (width / targetWidth)
        let size = CGSize(width: targetWidth, height: targetHeight)
        
        UIGraphicsBeginImageContext(size)
        let thumbnailRect = CGRect(origin: .zero, size: size)
        sourceImage.draw(in: thumbnailRect)
        
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        if newImage == nil {
            print("Failed to scale image")
        }
        
        return newImage
    }
}
