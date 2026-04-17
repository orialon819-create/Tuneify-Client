package com.example.tuneify_final_project.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig
import java.net.Socket
import java.net.InetSocketAddress

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth_splash)

        Thread {
            val isServerUp = try {
                val socket = Socket()
                // Use the shared IP from NetworkConfig
                socket.connect(InetSocketAddress(NetworkConfig.serverIp, NetworkConfig.serverPort), 2000)
                socket.close()
                true
            } catch (e: Exception) {
                false
            }

            runOnUiThread {
                if (isServerUp) {
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    startActivity(Intent(this, ServerSettingsActivity::class.java))
                }
                finish()
            }
        }.start()
    }
}