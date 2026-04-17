package com.example.tuneify_final_project.ui.auth

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuneify_final_project.R
import com.example.tuneify_final_project.ui.SocketManager
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_register)

        val fNameInput = findViewById<EditText>(R.id.reg_firstname)
        val lNameInput = findViewById<EditText>(R.id.reg_lastname)
        val emailInput = findViewById<EditText>(R.id.reg_email)
        val userInput = findViewById<EditText>(R.id.reg_username)
        val passInput = findViewById<EditText>(R.id.reg_password)
        val btnRegister = findViewById<Button>(R.id.btn_register_submit)
        val backToLoginText = findViewById<TextView>(R.id.back_to_login)

        setupSignInLink(backToLoginText)

        btnRegister.setOnClickListener {
            val params = JSONObject().apply {
                put("first_name", fNameInput.text.toString().trim())
                put("last_name", lNameInput.text.toString().trim())
                put("email", emailInput.text.toString().trim())
                put("username", userInput.text.toString().trim())
                put("password", passInput.text.toString().trim())
            }

            // Check if any field is empty
            if (params.getString("username").isEmpty() || params.getString("password").isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Refactored: Using SocketManager
            SocketManager.sendCommand("REGISTER", params) { response ->
                if (response != null && response.startsWith("OK")) {
                    Toast.makeText(this, "Account Created! Please login.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: $response", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupSignInLink(textView: TextView) {
        val text = "Already have an account? Sign in"
        val spannable = SpannableString(text)
        val start = text.indexOf("Sign in")
        val end = start + "Sign in".length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) { finish() }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}