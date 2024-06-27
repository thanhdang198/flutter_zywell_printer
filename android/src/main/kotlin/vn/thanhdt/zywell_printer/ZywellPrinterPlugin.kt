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
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
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
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.posprinterface.ProcessData
import net.posprinter.posprinterface.TaskCallback
import net.posprinter.service.PosprinterService
import net.posprinter.utils.DataForSendToPrinterTSC
import net.posprinter.utils.PosPrinterDev
import net.posprinter.utils.StringUtils
import java.util.Locale
import kotlin.math.roundToInt

/** ZywellPrinterPlugin */
class ZywellPrinterPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    var ISCONNECT = false
    var myBinder: IMyBinder? = null
    private val portType = 0 //0 net，1 ble，2  USB
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var applicationContext: Context? = null

    private var mSerconnection: ServiceConnection?=null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = flutterPluginBinding.applicationContext
        this.flutterPluginBinding = flutterPluginBinding
        //bind service，get imyBinder
        val intent = Intent(flutterPluginBinding.applicationContext, PosprinterService::class.java)
        mSerconnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                myBinder = service as IMyBinder
                Log.e("myBinder", "connect")
            }

            override fun onServiceDisconnected(name: ComponentName) {
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
                val ip : String = call.arguments as String
//            val ip: String = "192.168.0.207"
            connectNet(ip, result)
        }
        if (call.method =="disconnect"){
            disConnect()
        }
        if (call.method =="getPlatformVersion"){
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        }
        if(call.method =="printText"){
            val data:ArrayList<HashMap<String,Any>>? = call.argument("printData")

            val invoiceWidth = call.argument<Double>("invoiceWidth")!!
            val invoiceHeight = call.argument<Double>("invoiceHeight")!!
            println(data)
            if(data != null){
                printText(data,invoiceWidth,invoiceHeight)
            }
        }
        if(call.method == "connectBluetooth"){
//            val btAdress: String = "00:15:83:00:91:0A"
            val btAdress: String = call.arguments as String
            connectBT(btAdress)
        }
        if(call.method == "connectUSB"){
            val usbAddress: String = call.arguments as String
            connectUSB(usbAddress)
        }
        else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    fun connectPrinterWifi(ip: String) {
        val printer: PosPrinterDev.PrinterInfo
        printer = PosPrinterDev.PrinterInfo("PrinterName", PosPrinterDev.PortType.Ethernet, ip)

        //printer4=new PosPrinterDev.PrinterInfo("printer1", PosPrinterDev.PortType.Ethernet,"192.168.3.223");
        //printer4=new PosPrinterDev.PrinterInfo("printer1", PosPrinterDev.PortType.Ethernet,"192.168.3.223");
        AddPrinter(printer)
    }


    private fun AddPrinter(printer: PosPrinterDev.PrinterInfo) {

        myBinder?.AddPrinter(printer, object : TaskCallback {
            override fun OnSucceed() {
                Toast.makeText(
                    getZWApplicationContext(),
                    "Connect success", // getString(R.string.con_success),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun OnFailed() {
                Toast.makeText(
                    getZWApplicationContext(),
                    "Connect failed", // getString(R.string.con_failed),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    fun getZWApplicationContext(): Context {
        return flutterPluginBinding!!.applicationContext
    }

    private fun connectNet(ip: String,result: Result) {
        Log.d("connectNet", "connectNet: $ip")
        if (ISCONNECT) {
            Log.d("connectNet", "ISCONNECT: $ISCONNECT")
            myBinder?.DisconnectCurrentPort(object : TaskCallback {
                override fun OnSucceed() {
                    Log.d("connectNet", "OnSucceeds: ")
                }
                override fun OnFailed() {
                    Log.d("connectNet", "OnFaileds: ")
                }
            })
        } else {
            Log.d("connectNet", "ISCONNECT: $ISCONNECT")

            myBinder?.ConnectNetPort(ip, 9100, object : TaskCallback {
                override fun OnSucceed() {
                    ISCONNECT = true
//                        result.success("CONNECTED")
                    Log.d("connectNet", "OnSucceed: ")
                    Toast.makeText(
                        getZWApplicationContext(),
                        "Connect success", // getString(R.string.con_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    //                    myBinder.Acceptdatafromprinter(new TaskCallback() {
//                        @Override
//                        public void OnSucceed() {
//                            Log.e( "OnSucceed: ","读取成功");
//                        }
//
//                        @Override
//                        public void OnFailed() {
//                            Log.e( "OnSucceed: ","读取失败");
//                        }
//                    },50);
//                    myBinder?.CheckLinkedState(new TaskCallback() {
//                        @Override
//                        public void OnSucceed() {
//                            Log.e( "OnSucceed: ","打印机在线" );
//                        }
//
//                        @Override
//                        public void OnFailed() {
//                            Log.e( "OnSucceed: ","打印机离线" );
//                        }
//                    });
                }

                override fun OnFailed() {
                    ISCONNECT = false
                    Log.d("connectNet", "OnFailed: ")
//                        result.error("UNAVAILABLE", "Unavailable", null)
                    Toast.makeText(
                        getZWApplicationContext(),
                        "Connect fail", // getString(R.string.con_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    /*
       * 字节数组转16进制字符串
       */
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
    private fun connectBT(btAdress: String) {
        if (btAdress == null || btAdress == "") {
            Toast.makeText(
                getZWApplicationContext(),
                "Connect failed ", // getString(R.string.con_failed),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            myBinder?.ConnectBtPort(btAdress, object : TaskCallback {
                override fun OnSucceed() {
                    ISCONNECT = true
                    Toast.makeText(
                        getZWApplicationContext(),
                        "Connect success", // getString(R.string.con_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun OnFailed() {
                    ISCONNECT = false
                    Toast.makeText(
                        getZWApplicationContext(),
                        "Connect failed", // getString(R.string.con_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
            myBinder?.ConnectUsbPort(
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
    private fun disConnect() {
        if (ISCONNECT) {
            myBinder?.DisconnectCurrentPort(object : TaskCallback {
                override fun OnSucceed() {
                    ISCONNECT = false
                    Toast.makeText(getZWApplicationContext(), "disconnect ok", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun OnFailed() {
                    ISCONNECT = true
                    Toast.makeText(getZWApplicationContext(), "disconnect failed", Toast.LENGTH_SHORT)
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


    private fun printContent() {
        if (ISCONNECT) {
            myBinder?.WriteSendData(object : TaskCallback {
                override fun OnSucceed() {
                    Toast.makeText(
                        getZWApplicationContext(),
                        "Send success", // getString(R.string.send_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun OnFailed() {
                    Toast.makeText(
                        getZWApplicationContext(),
                        "Send failed", // getString(R.string.send_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, ProcessData {
                val list: MutableList<ByteArray> = java.util.ArrayList()
                //设置标签纸大小
                list.add(DataForSendToPrinterTSC.sizeBymm(50.0, 30.0))
                //设置间隙
                list.add(DataForSendToPrinterTSC.gapBymm(2.0, 0.0))
                //清除缓存
                list.add(DataForSendToPrinterTSC.cls())
                //设置方向
                list.add(DataForSendToPrinterTSC.direction(0))
                //线条
                list.add(DataForSendToPrinterTSC.bar(10, 10, 200, 3))
                //条码

                list.add(
                    DataForSendToPrinterTSC.barCode(
                        10,
                        45,
                        "128",
                        100,
                        1,
                        0,
                        2,
                        2,
                        "abcdef12345"
                    )
                )
                //文本,简体中文是TSS24.BF2,可参考编程手册中字体的代号
                list.add(
                    DataForSendToPrinterTSC.text(
                        220,
                        10,
                        "TSS24.BF2",
                        0,
                        1,
                        1,
                        "这是测试文本"
                    )
                )
                //打印
                list.add(DataForSendToPrinterTSC.print(1))
                list
            })
        } else {
            Toast.makeText(
                getZWApplicationContext(),
                "Please connect first", // getString(R.string.connect_first),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //打印文本
    private fun printText(data:ArrayList<HashMap<String,Any>>,invoiceWidth:Double,invoiceHeight:Double) {

        if (ISCONNECT) {
            Log.d("Printer Status",myBinder?.GetPrinterStatus().toString())

            myBinder?.WriteSendData(object : TaskCallback {
                override fun OnSucceed() {
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
                val list: MutableList<ByteArray> = java.util.ArrayList()

                list.add(DataForSendToPrinterTSC.initialPrinter())

                //设置标签纸大小
                list.add(DataForSendToPrinterTSC.sizeBymm(invoiceWidth, invoiceHeight))
                //设置间隙
                list.add(DataForSendToPrinterTSC.gapBymm(10.0, 0.0))
                //清除缓存
                list.add(DataForSendToPrinterTSC.cls())
                //设置方向
                list.add(DataForSendToPrinterTSC.direction(0))
                for (i in data.indices) {
                    val item = data[i]
                    val content = item["text"] as String
                    val x = item["paddingLeft"] as Double
                    val y = item["paddingToTopOfInvoice"] as Double
                    val font = item["font"] as String
                    when (item["contentType"] as String) {
                        "text" -> {
                            list.add(DataForSendToPrinterTSC.text(x.roundToInt(), y.roundToInt(), font, 0, 1, 1, content))
                        }
                        "barcode" -> {
                            list.add(DataForSendToPrinterTSC.barCode(x.roundToInt(), y.roundToInt(), "128", item["height"] as Int, 1, 0, 2, 2, content))}
                        "qr" -> {
                            list.add(DataForSendToPrinterTSC.qrCode(x.roundToInt(), y.roundToInt(), "M", 3, "A", 0, "M1", "S3", content))
                        }
                    }

                }

                list.add(DataForSendToPrinterTSC.print(1))
                list.add(DataForSendToPrinterTSC.eop())

//                list.add(DataForSendToPrinterTSC.feed(1))
                list.add(DataForSendToPrinterTSC.cut())
//                list.add(DataForSendToPrinterTSC.cls())
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

    private fun printBarcode() {
        if (ISCONNECT) {

            myBinder?.WriteSendData(object : TaskCallback {
                override fun OnSucceed() {
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
                val list: MutableList<ByteArray> = java.util.ArrayList()
                //设置标签纸大小
                list.add(DataForSendToPrinterTSC.sizeBymm(50.0, 30.0))
                //设置间隙
                list.add(DataForSendToPrinterTSC.gapBymm(2.0, 0.0))
                //清除缓存
                list.add(DataForSendToPrinterTSC.cls())
                //设置方向
                list.add(DataForSendToPrinterTSC.direction(0))
                //线条
                //                    list.add(DataForSendToPrinterTSC.bar(10,10,200,3));
                //条码
                list.add(
                    DataForSendToPrinterTSC.barCode(
                        10,
                        15,
                        "128",
                        100,
                        1,
                        0,
                        2,
                        2,
                        "abcdef12345"
                    )
                )
                //文本
                //                    list.add(DataForSendToPrinterTSC.text(10,30,"1",0,1,1,"abcasdjknf"));
                //打印
                list.add(DataForSendToPrinterTSC.print(1))
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

    private fun printQR() {
        if (ISCONNECT) {
            myBinder?.WriteSendData(object : TaskCallback {
                override fun OnSucceed() {
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
                val list: MutableList<ByteArray> = java.util.ArrayList()
                //设置标签纸大小
                list.add(DataForSendToPrinterTSC.sizeBymm(50.0, 30.0))
                //设置间隙
                list.add(DataForSendToPrinterTSC.gapBymm(2.0, 0.0))
                //清除缓存
                list.add(DataForSendToPrinterTSC.cls())
                //设置方向
                list.add(DataForSendToPrinterTSC.direction(0))
                //具体参数值请参看编程手册
                list.add(
                    DataForSendToPrinterTSC.qrCode(
                        10,
                        20,
                        "M",
                        3,
                        "A",
                        0,
                        "M1",
                        "S3",
                        "123456789"
                    )
                )
                //打印
                list.add(DataForSendToPrinterTSC.print(1))
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

    private fun printbitmap() {
//        val bitmap1 = BitmapProcess.compressBmpByYourWidth(
//            BitmapFactory.decodeResource(
//               flutterPluginBinding!!.applicationContext.resources,
//                R.drawable
//            ), 400
//        )
        myBinder?.WriteSendData(object : TaskCallback {
            override fun OnSucceed() {
                Toast.makeText(
                    getZWApplicationContext(),
//                    getString(R.string.send_success),
                    "Send success",
                    Toast.LENGTH_SHORT

                ).show()
            }

            override fun OnFailed() {
                Toast.makeText(
                    getZWApplicationContext(),
//                    getString(R.string.send_failed),
                    "Send failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ProcessData {
            val list: MutableList<ByteArray> = java.util.ArrayList()
            //设置标签纸大小
            list.add(DataForSendToPrinterTSC.sizeBymm(80.0, 60.0))
            //设置间隙
            list.add(DataForSendToPrinterTSC.gapBymm(2.0, 0.0))
            //清除缓存
            list.add(DataForSendToPrinterTSC.cls())
//            list.add(
//                DataForSendToPrinterTSC.bitmap(
//                    10,
//                    10,
//                    0,
//                    bitmap1,
//                    BitmapToByteData.BmpType.Threshold
//                )
//            )
            list.add(DataForSendToPrinterTSC.print(1))

//                //2023.5.18 ding 测试同一张标签pdf使用两次发送
//                //设置标签纸大小
//                list.add(DataForSendToPrinterTSC.sizeBymm(80,100));
//                //设置间隙
//                list.add(DataForSendToPrinterTSC.gapBymm(0,0));
//                //清除缓存
//                list.add(DataForSendToPrinterTSC.cls());
//                list.add(DataForSendToPrinterTSC.bitmap(10,10,0,bitmap1, BitmapToByteData.BmpType.Threshold));
//                list.add(DataForSendToPrinterTSC.print(1));
//
//                list.add(DataForSendToPrinterTSC.sizeBymm(80,100));
//                list.add(DataForSendToPrinterTSC.gapBymm(0,0));
//                //清除缓存
//                list.add(DataForSendToPrinterTSC.cls());
//                list.add(DataForSendToPrinterTSC.bitmap(10,400,0,bitmap1, BitmapToByteData.BmpType.Threshold));
//                list.add(DataForSendToPrinterTSC.print(1));
            list
        })
    }

}
