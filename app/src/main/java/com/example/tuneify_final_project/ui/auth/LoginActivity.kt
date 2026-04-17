package com.example.tuneify_final_project.ui.auth

import android.content.Intent
import android.graphics.Color
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
import com.example.tuneify_final_project.ui.home.HomeActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_login)

        val usernameData = findViewById<EditText>(R.id.et_login_username)
        val passwordData = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.btn_login_submit)

        setupSignUpLink()
        setupForgotPasswordLink()

        btnLogin.setOnClickListener {
            val username = usernameData.text.toString().trim()
            val password = passwordData.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val params = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }

                // Refactored: Using SocketManager
                SocketManager.sendCommand("LOGIN", params) { response ->
                    if (response != null && response.startsWith("OK|")) {
                        try {
                            // 1. Extract the JSON string after "OK|"
                            val jsonData = response.substringAfter("OK|")
                            val userObj = JSONObject(jsonData)

                            // 2. Open SharedPreferences to store the info
                            val sharedPref = getSharedPreferences("TuneifyPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("USER_ID", userObj.optInt("id", -1))
                                putString("USER_FIRST_NAME", userObj.optString("first_name"))
                                putString("USER_LAST_NAME", userObj.optString("last_name"))
                                putString("USER_EMAIL", userObj.optString("email"))
                                putString("USERNAME", userObj.optString("username"))
                                apply()
                            }

                            Toast.makeText(this, "Welcome back, ${userObj.getString("first_name")}!", Toast.LENGTH_SHORT).show()

                            // 3. Move to HomeActivity
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Login Failed: $response", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSignUpLink() {
        val tvGoToRegister = findViewById<TextView>(R.id.tv_go_to_register)
        val fullText = "Don't have an account? Sign up"
        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf("Sign up")
        val end = start + "Sign up".length

        spannableString.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvGoToRegister.text = spannableString
        tvGoToRegister.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupForgotPasswordLink() {
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        val text = "Forgot Password?"
        val spannableString = SpannableString(text)

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
            }
        }, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvForgotPassword.text = spannableString
        tvForgotPassword.movementMethod = LinkMovementMethod.getInstance()
    }
}