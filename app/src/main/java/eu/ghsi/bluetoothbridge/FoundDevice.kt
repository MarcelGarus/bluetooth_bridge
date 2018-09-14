package eu.ghsi.bluetoothbridge

import android.bluetooth.BluetoothDevice

data class FoundDevice(val device: BluetoothDevice, var lastSeen: Long, var lastSentToServer: Long) {
    override fun toString(): String { return device.toString() }
}
