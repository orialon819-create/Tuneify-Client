package com.example.tuneify_final_project.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_forgot_password)

        val etEmail = findViewById<EditText>(R.id.forgot_email)
        val btnSendCode = findViewById<Button>(R.id.btn_send_code)

        btnSendCode.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val params = JSONObject().apply { put("email", email) }

            // Refactored: Using SocketManager
            SocketManager.sendCommand("REQUEST_RESET", params) { response ->
                if (response != null && response.startsWith("OK")) {
                    Toast.makeText(this, "Verification code sent!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error: $response", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}