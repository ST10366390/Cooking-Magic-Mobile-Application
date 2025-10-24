package com.example.cookingmagic.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookingmagic.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class SignupActivity : AppCompatActivity() {

    private lateinit var signupViewBinding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        signupViewBinding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(signupViewBinding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle signup button click
        signupViewBinding.signupButton.setOnClickListener {
            signUpUser()
        }

        // Handle login redirect link click
        signupViewBinding.loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(signupViewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signUpUser() {
        val email = signupViewBinding.emailEditText.text.toString().trim()
        val password = signupViewBinding.passwordEditText.text.toString().trim()
        val confirmPassword = signupViewBinding.confirmPasswordEditText.text.toString().trim()

        // Validate inputs
        if (email.isEmpty()) {
            signupViewBinding.emailInputLayout.error = "Email is required"
            signupViewBinding.emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupViewBinding.emailInputLayout.error = "Please enter a valid email address"
            signupViewBinding.emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            signupViewBinding.passwordInputLayout.error = "Password is required"
            signupViewBinding.passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            signupViewBinding.passwordInputLayout.error = "Password must be at least 6 characters"
            signupViewBinding.passwordEditText.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            signupViewBinding.confirmPasswordInput.error = "Please confirm your password"
            signupViewBinding.confirmPasswordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            signupViewBinding.confirmPasswordInput.error = "Passwords do not match"
            signupViewBinding.confirmPasswordEditText.requestFocus()
            return
        }

        // Clear any previous errors
        signupViewBinding.emailInputLayout.error = null
        signupViewBinding.passwordInputLayout.error = null
        signupViewBinding.confirmPasswordInput.error = null

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign-up successful! Please log in.", Toast.LENGTH_SHORT).show()
                    // Navigate to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle specific Firebase errors
                    when (val exception = task.exception) {
                        is FirebaseAuthException -> {
                            when (exception.errorCode) {
                                "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                    signupViewBinding.emailInputLayout.error = "This email is already registered"
                                    signupViewBinding.emailEditText.requestFocus()
                                }
                                "ERROR_WEAK_PASSWORD" -> {
                                    signupViewBinding.passwordInputLayout.error = "Password is too weak"
                                    signupViewBinding.passwordEditText.requestFocus()
                                }
                                else -> {
                                    Toast.makeText(this, "Sign-up failed: ${exception.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this, "Sign-up failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }
}