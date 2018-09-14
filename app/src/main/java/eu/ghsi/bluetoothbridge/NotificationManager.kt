package eu.ghsi.bluetoothbridge

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast

class NotificationManager(private val service: BridgeService) {
    companion object {
        private const val STATE_ID = 1
        private const val WARNING_ID = 2
        private const val ERROR_ID = 3
    }


    /// Shows a notification that represents the state of the service.
    fun updateState(state: State) {
        val builder = Notification.Builder(service)
                .setContentText(when (state) {
                    State.STANDBY -> "Waiting <n> seconds until scanning" // TODO insert seconds left
                    State.SCANNING -> "Scanning for devices"
                    State.NOTIFY_SERVER -> "Offering <device name> to server" // TODO insert name
                    State.STOPPED -> "Shutting down"
                })
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(Color.BLUE)

        service.startForeground(STATE_ID, builder.build())
    }


    /// Shows an error notification. TODO: display as group notification
    fun showError(error: BridgeError) {
        println("Showing error notification because $error")
        val title = when (error) {
            BridgeError.BluetoothNotSupported -> "Bluetooth not supported"
            BridgeError.LocationPermissionMissing -> "Location Permission missing"
            BridgeError.BluetoothDisabled -> "Bluetooth disabled"
            BridgeError.BluetoothLowEnergyNotSupported -> "Bluetooth Low Energy not supported"
        }
        val details = when (error) {
            BridgeError.BluetoothNotSupported -> "Your device doesn't support Bluetooth."
            BridgeError.LocationPermissionMissing -> "Bluetooth Low Energy Beacons can reveal your location. Android requires apps to be granted location permission in order to use BLE."
            BridgeError.BluetoothDisabled -> "Bluetooth is currently disabled on this device."
            BridgeError.BluetoothLowEnergyNotSupported -> "Bluetooth Low Energy is not supported by this device."
        }
        val notificationManager = NotificationManagerCompat.from(service)
        val intent = Intent(service, MainActivity::class.java).apply {
            putExtra("error", error.toString())
        }
        println("Intent of notification is $intent with extras ${intent.extras}")

        val pendingIntent = PendingIntent.getActivity(service, 0, intent, 0)

        val builder = Notification.Builder(service)
                .setContentTitle(title)
                .setContentText(details)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(Color.RED)
                .setStyle(Notification.BigTextStyle().bigText(details))
                .setContentIntent(pendingIntent)
        notificationManager.notify(ERROR_ID, builder.build())

        service.stopSelf()
        service.state = State.STOPPED
    }


    /// Shows a warning notification. TODO: display as group notification
    fun showWarning(warning: String) {
        val notificationManager = NotificationManagerCompat.from(service)
        val builder = Notification.Builder(service)
                .setContentTitle("Warning")
                .setContentText(warning)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(Color.YELLOW)
                .setStyle(Notification.BigTextStyle().bigText(warning))
        notificationManager.notify(WARNING_ID, builder.build())
    }
}