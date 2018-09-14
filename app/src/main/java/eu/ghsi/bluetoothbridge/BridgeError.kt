package eu.ghsi.bluetoothbridge

/// Errors that can occur during the execution of the app.
enum class BridgeError {
    BluetoothNotSupported, // The device does not support Bluetooth. TODO: rm and add in Manifest
    LocationPermissionMissing, // The app has no access to the device's location (required for BLE).
    BluetoothDisabled, // Bluetooth is disabled on this device.
    BluetoothLowEnergyNotSupported // BLE is not supported on this device. TODO: rm and add in Manifest
}
