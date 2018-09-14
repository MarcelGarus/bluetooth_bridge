# bluetooth bridge
Eine Bluetooth-Brücke zu Thermostaten.
Dieses Repository ist Teil des GHSi Castle Projekts.

Unter `app/src/main/java/eu/ghsi/bluetoothbridge/` finden sich die wichtigen Dateien.
Die Dateien des Projekts sind folgende:

- `BluetoothListener.kt` (zurzeit nicht benutzt): Ein Interface für Klassen, die über Bluetooth-Events informiert werden wollen.
- `BridgeError.kt`: Ein Enum mit möglichen Errors, die auftreten können.
- `BridgeService.kt`: Der Service, der im Hintergrund läuft und die eigentliche Arbeit verrichtet.
- `BridgeServiceAPI.kt` (zurzeit nicht benutzt): Ein Interface für Klassen, die über den Status des BridgeServices informiert werden wollen.
- `CommunicationThread.kt`: Handelt die Verbindung zum Castle-Server.
- `FoundDevice.kt`: Eine Klasse für gefundene Geräte.
- `MainActivity.kt`: Die Haupt-Activity der App.
- `NotificationManager.kt`: Verwaltet die Notifications der App. Wenn ein Service im Hintergrund läuft, muss immer eine Notification der App angezeigt werden, damit der Nutzer im Klaren darüber ist, dass die App läuft.
- `ScanAttempt.kt`: Ein Enum mit möglichen Ergebnissen eines Scans.
- `State.kt`: Ein Enum mit den möglichen globalen States der App.

