package com.example.cookingmagic.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookingmagic.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loginViewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginViewBinding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle login button click
        loginViewBinding.loginButton.setOnClickListener {
            signInUser()
        }

        // Handle signup redirect link click
        loginViewBinding.signupTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(loginViewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signInUser() {
        val email = loginViewBinding.emailEditText.text.toString().trim()
        val password = loginViewBinding.passwordEditText.text.toString().trim()

        // Validate inputs
        if (email.isEmpty()) {
            loginViewBinding.emailInputLayout.error = "Email is required"
            loginViewBinding.emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginViewBinding.emailInputLayout.error = "Please enter a valid email address"
            loginViewBinding.emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            loginViewBinding.passwordInputLayout.error = "Password is required"
            loginViewBinding.passwordEditText.requestFocus()
            return
        }

        // Clear any previous errors
        loginViewBinding.emailInputLayout.error = null
        loginViewBinding.passwordInputLayout.error = null

        // Sign in user with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle specific Firebase errors
                    when (val exception = task.exception) {
                        is FirebaseAuthException -> {
                            when (exception.errorCode) {
                                "ERROR_USER_NOT_FOUND" -> {
                                    loginViewBinding.emailInputLayout.error = "No account found with this email"
                                    loginViewBinding.emailEditText.requestFocus()
                                }
                                "ERROR_WRONG_PASSWORD" -> {
                                    loginViewBinding.passwordInputLayout.error = "Incorrect password"
                                    loginViewBinding.passwordEditText.requestFocus()
                                }
                                "ERROR_INVALID_EMAIL" -> {
                                    loginViewBinding.emailInputLayout.error = "Invalid email format"
                                    loginViewBinding.emailEditText.requestFocus()
                                }
                                else -> {
                                    Toast.makeText(this, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this, "Login failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }
}