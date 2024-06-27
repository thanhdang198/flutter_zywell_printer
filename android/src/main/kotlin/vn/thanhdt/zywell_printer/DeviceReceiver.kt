package vn.thanhdt.zywell_printer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat


class DeviceReceiver(
    deviceList_found: ArrayList<String>,
    adapter: ArrayAdapter<String>,
    listView: ListView
) :
    BroadcastReceiver() {
    private var deviceList_found = ArrayList<String>()
    private val adapter: ArrayAdapter<String>
    private val listView: ListView

    init {
        this.deviceList_found = deviceList_found
        this.adapter = adapter
        this.listView = listView
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (BluetoothDevice.ACTION_FOUND == action) {
            //搜索到的新设备
            val btd = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            //搜索没有配对过的蓝牙设备
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // only for gingerbread and newer versions
                if (ActivityCompat.checkSelfPermission(
                        context,
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
            if (btd!!.bondState != BluetoothDevice.BOND_BONDED) {
                if (!deviceList_found.contains(
                        """
                            ${btd.name}
                            ${btd.address}
                            """.trimIndent()
                    )
                ) {
                    deviceList_found.add(
                        """
                            ${btd.name}
                            ${btd.address}
                            """.trimIndent()
                    )
                    // Log.e("onReceive: ","fsdf" );
                    try {
                        adapter.notifyDataSetChanged()
//                        listView.notify()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
            //搜索结束
            if (listView.count == 0) {
                deviceList_found.add("None ble device found")
                try {
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

