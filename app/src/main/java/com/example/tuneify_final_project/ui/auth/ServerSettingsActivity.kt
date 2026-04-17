package com.example.tuneify_final_project.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.NetworkConfig

class ServerSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_settings)

        val etIp = findViewById<EditText>(R.id.et_ip_address)
        val btnConnect = findViewById<Button>(R.id.btn_save_connect)

        btnConnect.setOnClickListener {
            val enteredIp = etIp.text.toString()

            if (enteredIp.isNotEmpty()) {

                NetworkConfig.serverIp = enteredIp

                startActivity(Intent(this, SplashActivity::class.java))

                finish()
            } else {
                Toast.makeText(this, "Please enter an IP", Toast.LENGTH_SHORT).show()
            }
        }
    }
}