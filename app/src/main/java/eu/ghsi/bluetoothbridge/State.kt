package eu.ghsi.bluetoothbridge

enum class State {
    STANDBY, // The app is on standby.
    SCANNING, // The app currently scans for devices.
    NOTIFY_SERVER, // The app notifies the server.
    STOPPED // The app stopped.
}
