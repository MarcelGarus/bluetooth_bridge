package eu.ghsi.bluetoothbridge

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.bluetooth.BluetoothAdapter




class MainActivity : AppCompatActivity() {
    companion object {
        val REQUEST_GRANT_LOCATION_PERMISSION = 1;
        val REQUEST_ENABLE_BLUETOOTH = 2;
    }

    var running: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            running = !running
            Snackbar.make(view, "Server " + if (running) "started" else "stopped", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            val intent = Intent(this, BridgeService::class.java)
            if (running)
                startService(intent)
            else
                stopService(intent)
        }

        // If this activity is created due to a click on an error notification, provide more
        // information about the error or try to resolve it.
        var error: BridgeError? = null
        for (errorType in BridgeError.values())
            if (intent.getStringExtra("error") == errorType.toString())
                error = errorType

        println("MainActivity: Intent is $intent with extras ${intent.extras}")
        println("Error is $error")
        when (error) {
            BridgeError.LocationPermissionMissing -> {
                println("Requesting location permission")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_GRANT_LOCATION_PERMISSION)
            }
            BridgeError.BluetoothDisabled -> {
                println("Requesting enabling of bluetooth")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
            }
            else -> return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
