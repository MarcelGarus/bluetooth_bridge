package eu.ghsi.bluetoothbridge

import java.io.*
import java.net.Socket

internal class CommunicationThread(private val service: BridgeService, private val clientSocket: Socket) : Runnable {
    private var input: BufferedReader? = null

    init {
        try {
            this.input = BufferedReader(InputStreamReader(this.clientSocket.getInputStream()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                val read = input!!.readLine()
                if (read == null) {
                    Thread.currentThread().interrupt()
                } else {
                    val out = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
                    out.write("TstMsg")

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
