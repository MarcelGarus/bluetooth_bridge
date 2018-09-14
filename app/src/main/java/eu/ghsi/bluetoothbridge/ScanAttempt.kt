package eu.ghsi.bluetoothbridge

enum class ScanAttempt {
    SUCCESSFUL, BLUETOOTH_NOT_SUPPORTED, LOCATION_PERMISSION_MISSING,
    BLUETOOTH_NOT_ENABLED, BLUETOOTH_LOW_ENERGY_NOT_SUPPORTED
}