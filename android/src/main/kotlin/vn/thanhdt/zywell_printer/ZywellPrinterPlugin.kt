package vn.thanhdt.zywell_printer

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import net.posprinter.posprinterface.PrinterBinder
import net.posprinter.posprinterface.ProcessData
import net.posprinter.posprinterface.TaskCallback
import net.posprinter.service.PosprinterService
import net.posprinter.service.PrinterConnectionsService
import net.posprinter.utils.BitmapProcess
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos58
import net.posprinter.utils.DataForSendToPrinterPos80
import net.posprinter.utils.DataForSendToPrinterTSC
import net.posprinter.utils.PosPrinterDev
import net.posprinter.utils.StringUtils
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt


/** ZywellPrinterPlugin */
class ZywellPrinterPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    var ISCONNECT = false
    var printerBinder: PrinterBinder? = null
    private val portType = 0 //0 net，1 ble，2  USB
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var applicationContext: Context? = null
    private var address: String = ""
    private var mSerconnection: ServiceConnection? = null
    private var firstPrint = false
    private var printCount =1;
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = flutterPluginBinding.applicationContext
        this.flutterPluginBinding = flutterPluginBinding
        //bind service，get imyBinder
        val intent =
            Intent(flutterPluginBinding.applicationContext, PrinterConnectionsService::class.java)
        mSerconnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is PrinterBinder)
                    printerBinder = service as PrinterBinder
                Log.e("myBinder", "connect")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                printerBinder = null
                Log.e("myBinder", "disconnect")
            }
        }
        flutterPluginBinding.applicationContext.bindService(
            intent,
            mSerconnection!!,
            BIND_AUTO_CREATE
        )

        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zywell_printer")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "connectIp") {
//            result.success("Android ${android.os.Build.VERSION.RELEASE}")
            val ip: String = call.arguments as String
//            val ip: String = "192.168.0.207"
            address = ip
            connectNet(ip, result)
        }
        if (call.method == "disconnect") {

            disConnect(address)
        }
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        }
        if (call.method == "printText") {
            val data: ArrayList<HashMap<String, Any>>? = call.argument("printData")

            val invoiceWidth = call.argument<Double>("invoiceWidth")!!
            val invoiceHeight = call.argument<Double>("invoiceHeight")!!
            println(data)
            if (data != null) {
                printText(data, invoiceWidth, invoiceHeight)
            }
        }
        if (call.method == "connectBluetooth") {
//            val btAdress: String = "00:15:83:00:91:0A"
            val btAdress: String = call.arguments as String
            connectBT(btAdress, result)
        }
        if (call.method == "connectUSB") {
            val usbAddress: String = call.arguments as String
            connectUSB(usbAddress)
        }
        if(call.method == "printImage"){
            val imagePath: String = call.arguments as String
            printPicture(imagePath, call, result)
        }
        else {
            println(call.method)
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    fun getZWApplicationContext(): Context {
        return flutterPluginBinding!!.applicationContext
    }

    private fun connectNet(ipAddress: String, result: Result) {

        if (ipAddress !== "") {
            val isConnected = printerBinder!!.isConnect(ipAddress)
            Log.d(
                "isConnected", "readBuffer ip: $ipAddress".toString() +
                        " isConnected: " + isConnected + ""
            )
            if (isConnected) {
                printerBinder!!.disconnectCurrentPort(ipAddress, object : TaskCallback {
                    override fun OnSucceed() {
                        printerBinder!!.connectNetPort(ipAddress, object : TaskCallback {
                            override fun OnSucceed() {
                                Log.d("ip_address", "readBuffer ip: $ipAddress")

//                                result.success(true)
                            }

                            override fun OnFailed() {
//                                result.success(false)
                            }
                        })
                    }

                    override fun OnFailed() {
//                        result.success(false)
                    }
                })
            } else {
                printerBinder!!.connectNetPort(ipAddress, object : TaskCallback {
                    override fun OnSucceed() {
//                        result.success(true)
                        ISCONNECT = true
                        Toast.makeText(getZWApplicationContext(), "Connected", Toast.LENGTH_SHORT)
                            .show()

                    }

                    override fun OnFailed() {
//                        result.success(false)
                    }
                })
            }
        } else {
//            result.success(false)
        }
    }

    fun bytes2HexString(array: ByteArray): String? {
        val builder = StringBuilder()
        for (b in array) {
            var hex = Integer.toHexString(b.toInt() and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            builder.append(hex)
        }
        return builder.toString().uppercase(Locale.getDefault())
    }

    /**
     * Android12 To use Bluetooth devices in the future, you need to obtain Bluetooth_SCAN and Bluetooth_CONNECT permissions, otherwise it will crash
     * update ding
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun reqBlePermission() {
        if (ActivityCompat.checkSelfPermission(
                getZWApplicationContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                getZWApplicationContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//            ActivityCompat.requestPermissions(
//                getActivity(),
//                arrayOf<String>(
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ),
//                1024
//            )
        }
    }

    /**
     * connect bluetooth
     */
    private fun connectBT(address: String, result: Result) {
        if (address !== "") {
            printerBinder!!.connectBtPort(address, object : TaskCallback {
                override fun OnSucceed() {
                    result.success(true)
                }

                override fun OnFailed() {
                    result.success(false)
                }
            })
        } else {
            result.success(false)
        }
    }

    /**
     * get usb permission
     */

    private var mUsbManager: UsbManager? = null
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"


    private fun afterGetUsbPermission(usbDevice: UsbDevice) {
        //call method to set up device communication
        //Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();
        doYourOpenUsbDevice(usbDevice)
    }

    private fun doYourOpenUsbDevice(usbDevice: UsbDevice) {
        //now follow line will NOT show: User has not given permission to device UsbDevice
        val connection = mUsbManager!!.openDevice(usbDevice)
        //add your operation code here
    }

    private val mUsbPermissionActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbDevice =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        usbDevice?.let { afterGetUsbPermission(it) }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(
                            context,
                            "Permission denied for device$usbDevice",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    /**
     * connect usb
     */
    private fun connectUSB(usbAddress: String) {
        if (usbAddress == null || usbAddress == "") {
            Toast.makeText(getZWApplicationContext(), "disconnected", Toast.LENGTH_SHORT)
                .show()
        } else {
            printerBinder?.connectUsbPort(
                getZWApplicationContext(),
                usbAddress,
                object : TaskCallback {
                    override fun OnSucceed() {
                        ISCONNECT = true
                        Toast.makeText(
                            getZWApplicationContext(),
                            "Connect", // getString(R.string.connect),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    override fun OnFailed() {
                        ISCONNECT = false
                        Toast.makeText(
                            getZWApplicationContext(),
                            "Disconnect", // getString(R.string.discon),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        }
    }

    /**
     * disconnect
     */
    private fun disConnect(ip: String) {
        if (ISCONNECT) {
            printerBinder?.disconnectCurrentPort(ip, object : TaskCallback {
                override fun OnSucceed() {
                    ISCONNECT = false
                    Toast.makeText(getZWApplicationContext(), "disconnect ok", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun OnFailed() {
                    ISCONNECT = true
                    Toast.makeText(
                        getZWApplicationContext(),
                        "disconnect failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        }
    }


    private val btList: MutableList<String> = ArrayList()
    private val btFoundList = ArrayList<String>()
    private var BtBoudAdapter: ArrayAdapter<String>? =
        null;
    private var BtfoundAdapter: ArrayAdapter<kotlin.String?>? = null
    private var BtDialogView: View? = null
    private var BtBoundLv: ListView? = null;
    private var BtFoundLv: android.widget.ListView? = null
    private var ll_BtFound: LinearLayout? = null
    private var btdialog: AlertDialog? = null
    private var btScan: Button? = null
    private var BtReciever: DeviceReceiver? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    /*
      select bluetooth device
       */

    /*
      select bluetooth device
       */
    fun setBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter!!.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        getZWApplicationContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
            }
            //request grant
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(intent, 1)
        } else {
            showblueboothlist()
        }
    }

    private fun showblueboothlist() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // only for gingerbread and newer versions
            if (ActivityCompat.checkSelfPermission(
                    getZWApplicationContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
        }
        if (!bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.startDiscovery()
        }
        val inflater = LayoutInflater.from(getZWApplicationContext())

        //Register Bluetooth Broadcast Receiver
        val filterStart = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filterEnd = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//        registerReceiver(BtReciever, filterStart)
//        registerReceiver(BtReciever, filterEnd)
        setDlistener()
        findAvalibleDevice()
    }

    private fun setDlistener() {
        // TODO Auto-generated method stub
        btScan!!.setOnClickListener {
            // TODO Auto-generated method stub
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(
                    getZWApplicationContext(),
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(
//                        getApplicationContext(),
//                        permission
//                    )
//                ) {
//                } else {
//                    ActivityCompat.requestPermissions(
//                        getApplicationContext(),
//                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                        1024
//                    )
//                }
            }
            ll_BtFound!!.visibility = View.VISIBLE
            //btn_scan.setVisibility(View.GONE);
        }
        //Click-to-connect for paired devices
        BtBoundLv!!.onItemClickListener =
            OnItemClickListener { arg0, arg1, arg2, arg3 ->
                // TODO Auto-generated method stub
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(
                                getZWApplicationContext(),
                                Manifest.permission.BLUETOOTH_SCAN
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return@OnItemClickListener
                        }
                    }
                    if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering) {
                        bluetoothAdapter!!.cancelDiscovery()
                    }
                    var mac = btList[arg2]
                    mac = mac.substring(mac.length - 17)
                    //                    String name=msg.substring(0, msg.length()-18);
                    //lv1.setSelection(arg2);
                    btdialog!!.cancel()
//                    adrress.setText(mac)
                    //Log.i("TAG", "mac="+mac);
                } catch (e: Exception) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
        //Unpaired devices, click, pair, and then connect
        BtFoundLv?.setOnItemClickListener(OnItemClickListener { arg0, arg1, arg2, arg3 ->
            // TODO Auto-generated method stub
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            getZWApplicationContext(),
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return@OnItemClickListener
                    }
                }
                if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering) {
                    bluetoothAdapter!!.cancelDiscovery()
                }
                val mac: String
                val msg = btFoundList[arg2]
                mac = msg.substring(msg.length - 17)
                val name = msg.substring(0, msg.length - 18)
                //lv2.setSelection(arg2);
                btdialog!!.cancel()
//                adrress.setText(mac)
                Log.i("TAG", "mac=$mac")
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        })
    }

    /*
      Find connectable bluetooth devices
       */
    private fun findAvalibleDevice() {
        // TODO Auto-generated method stub
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    getZWApplicationContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
        }
        val device = bluetoothAdapter!!.bondedDevices
        btList.clear()
        if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering) {
            BtBoudAdapter!!.notifyDataSetChanged()
        }
        if (device.size > 0) {
            //There are already paired Bluetooth devices
            val it: Iterator<BluetoothDevice> = device.iterator()
            while (it.hasNext()) {
                val btd = it.next()
                btList.add(
                    """
                ${btd.name}
                ${btd.address}
                """.trimIndent()
                )
                BtBoudAdapter!!.notifyDataSetChanged()
            }
        } else {
            btList.add("No can be matched to use bluetooth")
            BtBoudAdapter!!.notifyDataSetChanged()
        }
    }


    var dialogView3: View? = null
    private var tv_usb: TextView? = null
    private var usbList: List<String>? =
        null;
    private var usblist: kotlin.collections.MutableList<kotlin.String?>? = null
    private var lv_usb: ListView? = null
    private var adapter3: ArrayAdapter<String>? = null


    private fun setUSB() {
        usbList = PosPrinterDev.GetUsbPathNames(getZWApplicationContext())
        if (usbList == null) {
            usbList = ArrayList()
        }
    }


//    private fun printContent() {
//        if (ISCONNECT) {
//            printerBinder?.WriteSendData(object : TaskCallback {
//                override fun OnSucceed() {
//                    Toast.makeText(
//                        getZWApplicationContext(),
//                        "Send success", // getString(R.string.send_success),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                override fun OnFailed() {
//                    Toast.makeText(
//                        getZWApplicationContext(),
//                        "Send failed", // getString(R.string.send_failed),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }, ProcessData {
//                val list: MutableList<ByteArray> = java.util.ArrayList()
//                //设置标签纸大小
//                list.add(DataForSendToPrinterTSC.sizeBymm(50.0, 30.0))
//                //设置间隙
//                list.add(DataForSendToPrinterTSC.gapBymm(2.0, 0.0))
//                //清除缓存
//                list.add(DataForSendToPrinterTSC.cls())
//                //设置方向
//                list.add(DataForSendToPrinterTSC.direction(0))
//                //线条
//                list.add(DataForSendToPrinterTSC.bar(10, 10, 200, 3))
//                //条码
//
//                list.add(
//                    DataForSendToPrinterTSC.barCode(
//                        10,
//                        45,
//                        "128",
//                        100,
//                        1,
//                        0,
//                        2,
//                        2,
//                        "abcdef12345"
//                    )
//                )
//                //文本,简体中文是TSS24.BF2,可参考编程手册中字体的代号
//                list.add(
//                    DataForSendToPrinterTSC.text(
//                        220,
//                        10,
//                        "TSS24.BF2",
//                        0,
//                        1,
//                        1,
//                        "这是测试文本"
//                    )
//                )
//                //打印
//                list.add(DataForSendToPrinterTSC.print(1))
//                list
//            })
//        } else {
//            Toast.makeText(
//                getZWApplicationContext(),
//                "Please connect first", // getString(R.string.connect_first),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

    private fun printPicture(imagePath: String, call: MethodCall, result: Result) {
//        val options = call.arguments as HashMap<*, *>
        val imageUri = Uri.parse(imagePath)
        val realPath = imageUri.path

        val size: Int = 100// options.getInt("size")
        val width: Int = 100//options.getInt("width")
        val isDisconnect = false
//            if (options.hasKey("is_disconnect")) {
//            options.getBoolean("is_disconnect")
//        } else {
//            false
//        }
        val isCutPaper = true
//            if (options.hasKey("cut_paper")) {
//            options.getBoolean("cut_paper")
//        } else {
//            true
//        }
        val mode = "THERMAL"
//        if (options.hasKey("mode")) {
//            options.getString("mode")
//        } else {
//            "THERMAL"
//        }

        val bitmap = BitmapFactory.decodeFile(realPath)
        if (mode == "LABEL") {
            val paper_size = 50
//                if (options.hasKey("paper_size")) {
//                options.getInt("paper_size")
//            } else {
//                50
//            }
            if (bitmap != null && address != null) {
                val bitmap1 =
                    BitmapProcess.compressBmpByYourWidth(bitmap, width)
                val bitmapToPrint: Bitmap = convertGreyImg(bitmap1)
                printerBinder!!.writeDataByYouself(
                    address,
                    object : TaskCallback {
                        override fun OnSucceed() {
                            result.success(true)
                            if (isDisconnect) {
                                val task: TimerTask = object : TimerTask() {
                                    override fun run() {
                                        printerBinder!!.disconnectCurrentPort(
                                            address, object : TaskCallback {
                                                override fun OnSucceed() {
                                                    Log.d(
                                                        "disconnectCurrentPort",
                                                        "disconnect success"
                                                    )
                                                }

                                                override fun OnFailed() {}
                                            })
                                    }
                                }
                                val timer = Timer()
                                timer.schedule(task, 1500)
                            }
                        }

                        override fun OnFailed() {
                            result.success(false)
                        }
                    }
                ) {
                    val list: MutableList<ByteArray> = ArrayList()
                    // 设置标签纸大小
                    list.add(
                        DataForSendToPrinterTSC.sizeBymm(
                            paper_size.toDouble(),
                            30.0
                        )
                    )
                    // 设置间隙
                    list.add(DataForSendToPrinterTSC.gapBymm(3.0, 0.0))
                    // 清除缓存
                    list.add(DataForSendToPrinterTSC.cls())
                    list.add(
                        DataForSendToPrinterTSC.bitmap(
                            -2, 10, 0, bitmapToPrint,
                            BitmapToByteData.BmpType.Threshold
                        )
                    )
                    list.add(DataForSendToPrinterTSC.print(1))
                    list
                }
            } else {
                result.success(false)
            }
        } else {
            if (bitmap != null && address != null) {
                val bitmap1 =
                    BitmapProcess.compressBmpByYourWidth(bitmap, width)
                val bitmapToPrint: Bitmap = convertGreyImg(bitmap1)
                printerBinder!!.writeDataByYouself(
                    address,
                    object : TaskCallback {
                        override fun OnSucceed() {
                            result.success(true)
                            if (isDisconnect) {
                                val task: TimerTask = object : TimerTask() {
                                    override fun run() {
                                        printerBinder!!.disconnectCurrentPort(
                                            address, object : TaskCallback {
                                                override fun OnSucceed() {
                                                    Log.d(
                                                        "disconnectCurrentPort",
                                                        "disconnect success"
                                                    )
                                                }

                                                override fun OnFailed() {}
                                            })
                                    }
                                }
                                val timer = Timer()
                                timer.schedule(task, 3000)
                            }
                        }

                        override fun OnFailed() {
                            result.success(false)
                        }
                    }
                ) {
                    val list: MutableList<ByteArray> = ArrayList()
                    if (size == 58) {
                        list.add(DataForSendToPrinterPos58.initializePrinter())
                    } else {
                        list.add(DataForSendToPrinterPos80.initializePrinter())
                    }
                    var blist: List<Bitmap?> =
                        ArrayList()
                    blist = BitmapProcess.cutBitmap(50, bitmapToPrint)
                    for (i in blist.indices) {
                        if (size == 58) {
                            list.add(
                                DataForSendToPrinterPos58.printRasterBmp(
                                    0, blist[i], BitmapToByteData.BmpType.Dithering,
                                    BitmapToByteData.AlignType.Left, width
                                )
                            )
                        } else {
                            list.add(
                                DataForSendToPrinterPos80.printRasterBmp(
                                    0, blist[i], BitmapToByteData.BmpType.Dithering,
                                    BitmapToByteData.AlignType.Left, width
                                )
                            )
                        }
                    }

                    if (size == 58 && isCutPaper) {
                        list.add(DataForSendToPrinterPos58.printAndFeedLine())
                    } else if (isCutPaper) {
                        list.add(DataForSendToPrinterPos80.printAndFeedLine())
                    }

                    if (size == 80 && isCutPaper) {
                        list.add(
                            DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(
                                0x42, 0x66
                            )
                        )
                    }
                    list
                }
            } else {
                result.success(false)
            }
        }
    }

    private fun printText(
        data: ArrayList<HashMap<String, Any>>,
        invoiceWidth: Double,
        invoiceHeight: Double
    ) {

        if (ISCONNECT) {
//            Log.d("Printer Status", printerBinder?.isConnect(address).toString())

            printerBinder?.writeDataByYouself(address, object : TaskCallback {
                override fun OnSucceed() {
                    firstPrint = true
                    Toast.makeText(
                        getZWApplicationContext(),
//                        getString(R.string.send_success),
                        "Send success",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                override fun OnFailed() {
                    Toast.makeText(
                        getZWApplicationContext(),
//                        getString(R.string.send_failed),
                        "Send failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, ProcessData {
//                val list: MutableList<ByteArray> = java.util.ArrayList()

                val list: MutableList<ByteArray> = ArrayList()
                list.add(DataForSendToPrinterPos58.initializePrinter())
                if (!firstPrint) {
                    list.add(DataForSendToPrinterTSC.sizeBymm(invoiceWidth, invoiceHeight))
                    list.add(DataForSendToPrinterTSC.gapBymm(10.0, 0.0))
                    list.add(DataForSendToPrinterTSC.cls())
                    list.add(DataForSendToPrinterTSC.direction(0))
                }
                for (i in data.indices) {
                    val item = data[i]
                    val content = item["text"] as String
                    val x = item["paddingLeft"] as Double
                    val y = item["paddingToTopOfInvoice"] as Double
                    val font = item["font"] as String
                    when (item["contentType"] as String) {
                        "PrintType.text" -> {
                            list.add(
                                DataForSendToPrinterTSC.text(
                                    x.roundToInt(),
                                    y.roundToInt(),
                                    font,
                                    0,
                                    1,
                                    1,
                                    content
                                )
                            )

//                            list.add(StringUtils.strTobytes(content));
                        }

                        "PrintType.barcode" -> {
                            list.add(
                                DataForSendToPrinterTSC.barCode(
                                    x.roundToInt(),
                                    y.roundToInt(),
                                    "128",
                                    item["height"] as Int,
                                    1,
                                    0,
                                    2,
                                    2,
                                    content
                                )
                            )
                        }

                        "PrintType.qr" -> {
                            list.add(
                                DataForSendToPrinterTSC.qrCode(
                                    x.roundToInt(),
                                    y.roundToInt(),
                                    "M",
                                    3,
                                    "A",
                                    0,
                                    "M1",
                                    "S3",
                                    content
                                )
                            )
                        }
                    }

                }

                list.add(DataForSendToPrinterTSC.print(1))
                list.add(DataForSendToPrinterTSC.eop())

//                list.add(DataForSendToPrinterTSC.feed(1))
                list.add(DataForSendToPrinterTSC.cut())
//                list.add(DataForSendToPrinterTSC.cls())

                list.add(DataForSendToPrinterTSC.print(printCount++));
                list.add(DataForSendToPrinterTSC.feed(printCount++));
//                list.add(DataForSendToPrinterPos80.printAndFeedLine())
                list
            })

        } else {
            Toast.makeText(
                getZWApplicationContext(),
//                getString(R.string.connect_first),
                "Connect first",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun convertGreyImg(img: Bitmap): Bitmap {
        val width = img.width
        val height = img.height

        val pixels = IntArray(width * height)

        img.getPixels(pixels, 0, width, 0, 0, width, height)

        // The arithmetic average of a grayscale image; a threshold
        var redSum = 0.0
        var greenSum = 0.0
        var blueSun = 0.0
        val total = (width * height).toDouble()

        for (i in 0 until height) {
            for (j in 0 until width) {
                val grey = pixels[width * i + j]

                val red = ((grey and 0x00FF0000) shr 16)
                val green = ((grey and 0x0000FF00) shr 8)
                val blue = (grey and 0x000000FF)

                redSum += red.toDouble()
                greenSum += green.toDouble()
                blueSun += blue.toDouble()
            }
        }
        val m = (redSum / total).toInt()

        // Conversion monochrome diagram
        for (i in 0 until height) {
            for (j in 0 until width) {
                var grey = pixels[width * i + j]

                val alpha1 = 0xFF shl 24
                var red = ((grey and 0x00FF0000) shr 16)
                var green = ((grey and 0x0000FF00) shr 8)
                var blue = (grey and 0x000000FF)

                if (red >= m) {
                    blue = 255
                    green = blue
                    red = green
                } else {
                    blue = 0
                    green = blue
                    red = green
                }
                grey = alpha1 or (red shl 16) or (green shl 8) or blue
                pixels[width * i + j] = grey
            }
        }
        val mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return mBitmap
    }

    fun readBuffer(ip: String?, result: Result) {
        if (ip != null) {
            val queue = printerBinder!!.readBuffer(ip)
            if (queue != null && queue.realSize() > 0) {
                // The queue is not empty
                val res = HashMap<String, Any>()
                res["queueSize"] = queue.realSize()
                result.success(result)
            } else {
                // The queue is empty
                result.error("EMPTY", "The queue is empty", null)
            }
        } else {
            result.error("InvalidArgument", "IP address is null", null)
        }
    }

    fun clearBuffer(ip: String?, result: Result) {
        if (ip != null) {
            printerBinder!!.clearBuffer(address)
            result.success(true)
        } else {
            result.error("InvalidArgument", "IP address is null", null)
        }
    }

    fun isConnect(ip: String?, result: Result) {
        if (ip != null) {
            val isConnected = printerBinder!!.isConnect(ip)
            result.success(isConnected)
        } else {
            result.error("InvalidArgument", "IP address is null", null)
        }
    }

    fun disconnectPort(ip: String?, result: Result) {
        if (ip != null) {
            printerBinder!!.disconnectCurrentPort(ip, object : TaskCallback {
                override fun OnSucceed() {
                    result.success(true)
                }

                override fun OnFailed() {
                    result.error(
                        "DisconnectFailed",
                        "Failed to disconnect the printer",
                        null
                    )
                }
            })
        } else {
            result.error("InvalidArgument", "IP address is null", null)
        }
    }

    fun disconnectAll(result: Result) {
        printerBinder!!.disconnectAll(object : TaskCallback {
            override fun OnSucceed() {
                result.success(true)
            }

            override fun OnFailed() {
                result.success(false)
            }
        })
    }
}
