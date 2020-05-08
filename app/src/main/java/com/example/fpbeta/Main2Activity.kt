package com.example.fpbeta

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*






class Main2Activity : AppCompatActivity() {
    // GUI Components

    private var mBluetoothStatus: TextView? = null
    private var mReadBuffer: TextView? = null
    private var mScanBtn: Button? = null
    private var mOffBtn: Button? = null
    private var mListPairedDevicesBtn: Button? = null
    private var mDiscoverBtn: Button? = null
    private var mBTAdapter: BluetoothAdapter? = null
    private var mPairedDevices: Set<BluetoothDevice>? = null
    private var mBTArrayAdapter: ArrayAdapter<String>? = null
    private var mDevicesListView: ListView? = null
    private var mLED1: Button? = null


    private var mLED2: Button? = null        // private var mLED2: CheckBox? = null

    var btSocket: BluetoothSocket? = null

    private var mHandler // Our main handler that will receive callback notifications
            : Handler? = null
    private var mConnectedThread // bluetooth background worker thread to send and receive data
            : ConnectedThread? = null
    private var mBTSocket: BluetoothSocket? = null // bi-directional client-to-client data path
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mBluetoothStatus = findViewById(R.id.bluetoothStatus) as TextView
        mReadBuffer = findViewById(R.id.readBuffer) as TextView
        mScanBtn = findViewById(R.id.scan) as Button
        mOffBtn = findViewById(R.id.off) as Button
        mDiscoverBtn = findViewById(R.id.discover) as Button
        mListPairedDevicesBtn = findViewById(R.id.PairedBtn) as Button
        mLED1 = findViewById(R.id.checkboxLED1) as Button

        mLED2 = findViewById(R.id.checkboxLED2) as Button     //mLED2 = findViewById(R.id.checkboxLED2) as CheckBox



        mBTArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio
        mDevicesListView = findViewById(R.id.devicesListView) as ListView
        mDevicesListView!!.adapter = mBTArrayAdapter // assign model to view
        mDevicesListView!!.onItemClickListener = mDeviceClickListener
        mHandler = Handler1()
        if (mBTArrayAdapter == null) { // Device does not support Bluetooth
            mBluetoothStatus!!.text = "Status: Bluetooth not found"
            Toast.makeText(
                applicationContext,
                "Bluetooth device not found!",
                Toast.LENGTH_SHORT
            ).show()
        }
        else  {
           /* mLED1!!.setOnClickListener {
                if (mConnectedThread != null) //First check to make sure thread created
                    mConnectedThread!!.write("1")
                Log.d(MainActivity.TAG, "1 und")
                                                 //Malayali ethi ethi                Radio Button Code

} check usage of this brace

                   mLED2!!.setOnClickListener {
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread!!.write2("7")
                    Log.d(MainActivity.TAG, "2 und")



                                        }
*/


            mLED1!!.setOnClickListener { v ->  if (mConnectedThread != null) //First check to make sure thread created
                mConnectedThread!!.write("1")}


            mLED2!!.setOnClickListener { v ->  if (mConnectedThread != null) //First check to make sure thread created
                mConnectedThread!!.write("7")}




            mScanBtn!!.setOnClickListener { v -> bluetoothOn(v) }
            mOffBtn!!.setOnClickListener { v -> bluetoothOff(v) }
            mListPairedDevicesBtn!!.setOnClickListener { v -> listPairedDevices(v) }
            mDiscoverBtn!!.setOnClickListener { v -> discover(v) }
            }

    }

    inner class Handler1 : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MESSAGE_READ) {
                var readMessage: String? = null
                try {
                    readMessage = String((msg.obj as ByteArray), Charset.forName("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                mReadBuffer!!.text = readMessage
            }
            if (msg.what == CONNECTING_STATUS) {
                if (msg.arg1 == 1) mBluetoothStatus!!.text =
                    "Connected to Device: " + msg.obj as String else mBluetoothStatus!!.text =
                    "Connection Failed"
            }
        }
    }

    private fun bluetoothOn(view: View) {
        if (!mBTAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            mBluetoothStatus!!.text = "Bluetooth enabled"
            Toast.makeText(applicationContext, "Bluetooth turned on", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(applicationContext, "Bluetooth is already on", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        Data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            Data
        ) // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) { // Make sure the request was successful




            if (resultCode == RESULT_OK) { // The user picked a contact.
// The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus!!.text = "Enabled"


                Toast.makeText(applicationContext, "Toggle mwounse", Toast.LENGTH_SHORT)
                    .show()

            } else mBluetoothStatus!!.text = "Disabled"
        }
    }

    private fun bluetoothOff(view: View) {
        mBTAdapter!!.disable() // turn off
        mBluetoothStatus!!.text = "Bluetooth disabled"
        Toast.makeText(applicationContext, "Bluetooth turned Off", Toast.LENGTH_SHORT).show()
    }

    private fun discover(view: View) { // Check if the device is already discovering
        if (mBTAdapter!!.isDiscovering) {
            mBTAdapter!!.cancelDiscovery()
            Toast.makeText(applicationContext, "Discovery stopped", Toast.LENGTH_SHORT).show()
        } else {
            if (mBTAdapter!!.isEnabled) {
                mBTArrayAdapter!!.clear() // clear items
                mBTAdapter!!.startDiscovery()
                Toast.makeText(applicationContext, "Discovery started", Toast.LENGTH_SHORT)
                    .show()
                registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            } else {
                Toast.makeText(applicationContext, "Bluetooth not on", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    val blReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(
                    BluetoothDevice.EXTRA_DEVICE
                )
                // add the name to the list
                mBTArrayAdapter!!.add(device.name + "\n" + device.address)
                mBTArrayAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun listPairedDevices(view: View) {
        mPairedDevices = mBTAdapter!!.bondedDevices
        if (mBTAdapter!!.isEnabled) { // put it's one to the adapter
            for (device in (mPairedDevices as MutableSet<BluetoothDevice>?)!!) mBTArrayAdapter!!.add(device.name + "\n" + device.address)
            Toast.makeText(applicationContext, "Show Paired Devices", Toast.LENGTH_SHORT)
                .show()
        } else Toast.makeText(
            applicationContext,
            "Bluetooth not on",
            Toast.LENGTH_SHORT
        ).show()
    }

    private val mDeviceClickListener =
        OnItemClickListener { av, v, arg2, arg3 ->
            if (!mBTAdapter!!.isEnabled) {
                Toast.makeText(baseContext, "Bluetooth not on", Toast.LENGTH_SHORT).show()
                return@OnItemClickListener
            }
            mBluetoothStatus!!.text = "Connecting..."
            // Get the device MAC address, which is the last 17 chars in the View
            val info = (v as TextView).text.toString()
            val address = info.substring(info.length - 17)
            val name = info.substring(0, info.length - 17)
            // Spawn a new thread to avoid blocking the GUI one
            object : Thread() {
                override fun run() {
                    var fail = false
                    val device = mBTAdapter!!.getRemoteDevice(address)
                    try {
                        mBTSocket = createBluetoothSocket(device)
                    } catch (e: IOException) {
                        fail = true
                        Toast.makeText(
                            baseContext,
                            "Socket creation failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket!!.connect()
                    } catch (e: IOException) {
                        try {
                            fail = true
                            mBTSocket!!.close()
                            mHandler!!.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget()
                        } catch (e2: IOException) { //insert code to deal with this
                            Toast.makeText(
                                baseContext,
                                "Socket creation failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = ConnectedThread(mBTSocket!!)
                        mConnectedThread!!.start()
                        mHandler!!.obtainMessage(
                            CONNECTING_STATUS,
                            1,
                            -1,
                            name
                        )
                            .sendToTarget()
                    }
                }
            }.start()
        }

    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
        //creates secure outgoing connection with BT device using UUID
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024) // buffer store for the stream
            var bytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try { // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)
                    if (bytes != 0) {
                        SystemClock.sleep(100)
                        mmInStream.read(buffer)
                    }
                    // Send the obtained bytes to the UI activity
                    mHandler!!.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
       fun write(input: String) {
           val bytes = input.toByteArray() //converts entered String into bytes
            try {
                mmOutStream!!.write(bytes)

                Log.d(MainActivity.TAG, " 1 write und")
            } catch (e: IOException) {
            }

        }


        fun write2(input: String) {
            val bytes = input.toByteArray() //converts entered String into bytes

            try {

                mmOutStream!!.write(bytes)
                Log.d(MainActivity.TAG, "2 write und")
            } catch (e: IOException) {
            }
        }







        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            // Get the input and output streams, using temp objects because
// member streams are final
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    companion object {
        private val BTMODULEUUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier
        // #defines for identifying shared types between calling functions
        private const val REQUEST_ENABLE_BT = 1 // used to identify adding bluetooth names
        private const val MESSAGE_READ = 2 // used in bluetooth handler to identify message update
        private const val CONNECTING_STATUS =
            3 // used in bluetooth handler to identify message status
    }
}

