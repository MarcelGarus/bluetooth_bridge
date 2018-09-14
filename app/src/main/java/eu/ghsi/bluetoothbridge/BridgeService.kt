package eu.ghsi.bluetoothbridge

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.IBinder
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import java.util.*
import android.support.v4.content.ContextCompat
import kotlin.collections.ArrayList
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.io.*


/**
 * Created by marcel on 3/10/18.
 * A service that handles the discovery, connection and communication with BLE devices.
 */

class BridgeService : Service() {
    companion object {
        //private val SERVER_PORT = 8080

        private const val STANDBY_PERIOD: Long = 20000 // 5 seconds
        private const val SCAN_PERIOD: Long = 10000 // 10 seconds

        private const val DEVICE_FORGET: Long = 60000 // 1 minute
        private const val SERVER_UPDATE: Long = 60000 // 1 minute

        private const val SERVER_HOST = "Pac11" //"10.98.1.154"
        private const val SERVER_PORT = 20170

        private val SERVICE_UUID = UUID.fromString("3e135142-654f-9090-134a-a6ff5bb77046")
        private val CHARACTERISTIC_CONTROL_UUID = UUID.fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09")
    }

    // The state of the service. Every change to the state automatically updates the notification.
    var state = State.SCANNING
        set(value) { field = value; notificationManager.updateState(value) }

    //private val binder = LocalBinder()
    private val notificationManager = NotificationManager(this)

    // Bluetooth stuff
    private val listeners = ArrayList<BluetoothListener>() // TODO: rm
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            println("Device scanned: ${result.device.name} - ${result.device.address}")
            val device = devices.singleOrNull { result.device.address == it.device.address }

            if (device == null)
                devices.add(FoundDevice(result.device, System.currentTimeMillis(), Long.MIN_VALUE))
            else
                device.lastSeen = System.currentTimeMillis()
        }

        // Scan failed
        override fun onScanFailed(errorCode: Int) {
            // TODO create a warning notification
        }
    }
    val devices = ArrayList<FoundDevice>()

    //private var gatt: BluetoothGatt? = null
    //private val gattCallback = ThermostatGattCallback()

    // TCP server stuff
    /*private var serverSocket: ServerSocket? = null
    var serverThread: Thread? = null*/


    override fun onCreate() {
        println("BridgeService created")

        notificationManager.updateState(state)

        Thread(Runnable {
            while (state != State.STOPPED) {
                when (state) {
                    State.STANDBY -> startStandby()
                    State.SCANNING -> startScan()
                    State.NOTIFY_SERVER -> notifyServer()
                    State.STOPPED -> {}
                }
            }
        }).start()
    }

    override fun onDestroy() {
        println("BridgeService destroyed")
        try {
            //serverSocket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onBind(p0: Intent?): IBinder {
        throw NotImplementedError("Not implemented yet.")
    }

    /// Waits for some time. Then, sets state to SCANNING.
    private fun startStandby() {
        Thread.sleep(STANDBY_PERIOD)
        state = State.SCANNING
    }

    /// Checks that location permission is granted and bluetooth is enabled, so we can start a BLE
    /// scan. Then, scans for some time and adds all scanned devices to the devices list. Finally,
    /// sets state to NOTIFY_SERVER.
    private fun startScan() {
        val bluetoothAdapter: BluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        // Make sure location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return notificationManager.showError(BridgeError.LocationPermissionMissing)

        // Make sure bluetooth is enabled
        if (!bluetoothAdapter.isEnabled)
            return notificationManager.showError(BridgeError.BluetoothDisabled)

        scanner = bluetoothAdapter.bluetoothLeScanner

        // Start scan and wait for some time. Then, stop the scan
        println("Starting scan")
        scanner!!.startScan(scanCallback)
        Thread.sleep(SCAN_PERIOD)
        scanner!!.stopScan(scanCallback)
        state = State.NOTIFY_SERVER
    }


    /// Notifies the server about every new device.
    private fun notifyServer() {
        val now = System.currentTimeMillis()
        devices.removeAll { it.lastSeen + DEVICE_FORGET < now }

        println("Notifying server about all new devices. All ${devices.size} devices are $devices")
        //devices.filter { it.lastSentToServer + SERVER_UPDATE < now }.forEach {
            try {
                println("INet address is ${InetAddress.getByName(SERVER_HOST).hostAddress}")
                println("Creating socket (connecting)")
                val socket = Socket(InetAddress.getByName(SERVER_HOST).hostAddress, SERVER_PORT)

                println("Opening output stream")
                Thread.sleep(1000)
                val outStream = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)


                // Challenge-Response TODO: come on
                //outStream.print('\0');
                //outStream.print("\0\0\0\0\0\0\0\0")

                /*outStream.println(devices.map {
                    "{\n" +
                        "address: \"${it.device.address}\",\n" +
                        "name: \"${it.device.name ?: ""}\",\n" +
                        "lastSeen: ${it.lastSeen}\n" +
                    "}\n"
                }.joinToString())*/
                /*outStream.println(
                    "{\n" +
                        "address: \"${it.device.address}\",\n" +
                        "name: \"${it.device.name ?: ""}\",\n" +
                        "lastSeen: ${it.lastSeen}\n" +
                    "}"
                )*/
                outStream.flush()
                val inStream = BufferedReader(InputStreamReader(socket.getInputStream()))
                val read = inStream.readLine()
                println("Answer from server: $read")
                notificationManager.showWarning("Answer from server: $read")

            } catch (e: UnknownHostException) {
                println("Unknown server $SERVER_HOST.")
                notificationManager.showWarning("Unknown server $SERVER_HOST.")
            } catch (e: IOException) {
                println("IOException during doing stuff with socket.")
                notificationManager.showWarning("IOException during doing stuff with socket.")
            } catch (e: Exception) {
                println("Exception during doing stuff with socket.")
                notificationManager.showWarning("Exception during doing stuff with socket.")
            }
        //}

        state = State.STANDBY
    }



    // Attempts to connect to a device.
    internal fun connect(device: BluetoothDevice) {
        if (state == State.SCANNING) {
            scanner!!.stopScan(scanCallback)
        }
        //Log.d(getClass().getSimpleName(), "Connecting to device " + device.name + " (" + device.address + ")")
        state = State.NOTIFY_SERVER
        for (listener in listeners)
            listener.onConnectionAttemptStarted(device)

        /*this.gatt = device.connectGatt(this, false, gattCallback)

        if (this.gatt == null) {
            state = State.STANDBY
            for (listener in listeners)
                listener.onConnectionAttemptFailed(device)
        }*/
    }


    // A binder that doesn't really do much except providing the option to get this service.
    /*internal inner class LocalBinder : Binder() {
        val service: BridgeService
            get() = this@BridgeService
    }*/


    // A gatt callback that checks the connected thermostat for the correct service and
    // characteristic.
    private inner class ThermostatGattCallback : BluetoothGattCallback() {
        override// A device changed the connection state
        fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            println("Device " + gatt.device + " changed connection state to " + newState)
            if (newState == BluetoothGatt.STATE_CONNECTED)
                gatt.discoverServices() // Start discovering services
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                state = State.STANDBY
                for (listener in listeners)
                    listener.onDisconnected(gatt.device)
            }
        }

        override// A device's services were discovered
        fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            //Log.d(javaClass.simpleName, gatt.services.size.toString() + " services discovered: ")
            //for (service in gatt.services)
            //    Log.d(javaClass.simpleName, "Service " + service.uuid)

            if (gatt.getService(SERVICE_UUID) == null || gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_CONTROL_UUID) == null)
                for (listener in listeners)
                    listener.onServiceMissing(gatt.device)
            else {
                for (listener in listeners)
                    listener.onConnected(gatt.device)
            }
        }
    }
}