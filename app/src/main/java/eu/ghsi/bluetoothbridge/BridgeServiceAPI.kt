package eu.ghsi.bluetoothbridge

import android.bluetooth.BluetoothDevice

// TODO: use or rm
interface BridgeServiceAPI {
    val state: State
    val error: Error

    fun onScanTimedOut()
    fun onScanFailed()
    fun onDeviceFound(device: BluetoothDevice)
    fun onConnectionAttemptStarted(device: BluetoothDevice)
    fun onConnectionAttemptFailed(device: BluetoothDevice)
    fun onServiceMissing(device: BluetoothDevice)
    fun onConnected(device: BluetoothDevice)
    fun onDisconnected(device: BluetoothDevice)
}
