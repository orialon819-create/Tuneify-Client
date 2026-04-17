package com.example.tuneify_final_project.ui

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

object SocketManager {

    /**
     * Sends a JSON command to the Python server.
     * @param command The string command (e.g., "LOGIN", "REGISTER")
     * @param parameters The JSONObject containing the data
     * @param onResponse A callback function that runs when the server answers
     */
    fun sendCommand(command: String, parameters: JSONObject, onResponse: (String?) -> Unit) {
        thread {
            var socket: Socket? = null
            try {
                // Connect using your shared NetworkConfig
                socket = Socket(NetworkConfig.serverIp, NetworkConfig.serverPort)
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Wrap in your protocol format
                val request = JSONObject()
                request.put("command", command)
                request.put("parameters", parameters)

                // Send and wait for 1 line of response
                writer.println(request.toString())
                val response = reader.readLine()

                // Crucial: Run the callback on the UI thread
                Handler(Looper.getMainLooper()).post {
                    onResponse(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    onResponse("ERROR|Connection failed")
                }
            } finally {
                socket?.close()
            }
        }
    }
}