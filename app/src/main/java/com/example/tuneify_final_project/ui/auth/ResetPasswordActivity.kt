package com.example.tuneify_final_project.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_reset_password)

        // Get the email passed from ForgotPasswordActivity
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        val etCode = findViewById<EditText>(R.id.et_reset_code)
        val etNewPassword = findViewById<EditText>(R.id.et_reset_new_password)
        val btnUpdate = findViewById<Button>(R.id.btn_update_password)

        btnUpdate.setOnClickListener {
            val code = etCode.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()

            if (code.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the refactored function
            verifyAndReset(userEmail, code, newPass)
        }
    }

    private fun verifyAndReset(email: String, code: String, newPass: String) {
        // 1. Prepare the parameters
        val params = JSONObject().apply {
            put("email", email)
            put("code", code)
            put("new_password", newPass)
        }

        // 2. Use the SocketManager to send the command
        SocketManager.sendCommand("VERIFY_RESET", params) { response ->
            // This part runs on the UI thread automatically thanks to SocketManager
            if (response != null && response.startsWith("OK")) {
                Toast.makeText(this, "Password updated! Please login.", Toast.LENGTH_LONG).show()
                finish() // Returns user to Login screen
            } else {
                val errorMsg = response?.substringAfter("ERROR|") ?: "Unknown error"
                Toast.makeText(this, "Invalid Code or Error: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        }
    }
}