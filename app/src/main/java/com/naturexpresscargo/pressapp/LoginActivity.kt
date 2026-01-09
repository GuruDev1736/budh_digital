package com.naturexpresscargo.pressapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.pressapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        auth = FirebaseAuth.getInstance()

        setupEdgeToEdge()
        setupListeners()
        setupEmailValidation()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.cardContainer.setPadding(
                0,
                0,
                0,
                insets.bottom
            )

            windowInsets
        }
    }

    private fun setupEmailValidation() {
        binding.ivEmailCheck.setImageResource(R.drawable.ic_email)
        binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && binding.etEmail.hasFocus()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        binding.ivEmailCheck.setImageResource(R.drawable.ic_check)
                        binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this@LoginActivity, R.color.status_completed))
                    } else {
                        binding.ivEmailCheck.setImageResource(R.drawable.ic_error)
                        binding.ivEmailCheck.clearColorFilter()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.ivEmailCheck.setImageResource(R.drawable.ic_email)
                binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))
            } else {
                val email = binding.etEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        binding.ivEmailCheck.setImageResource(R.drawable.ic_check)
                        binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.status_completed))
                    } else {
                        binding.ivEmailCheck.setImageResource(R.drawable.ic_error)
                        binding.ivEmailCheck.clearColorFilter()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivPasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnSignIn.setOnClickListener {
            handleLogin()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSignIn.isEnabled = false
        binding.btnSignIn.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnSignIn.isEnabled = true
                binding.btnSignIn.text = "Sign In"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}

