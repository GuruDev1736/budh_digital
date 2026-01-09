package com.naturexpresscargo.pressapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.pressapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
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
                        binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.status_completed))
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
        binding.btnResetPassword.setOnClickListener {
            handleResetPassword()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun handleResetPassword() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnResetPassword.isEnabled = false
        binding.btnResetPassword.text = "Sending..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.btnResetPassword.isEnabled = true
                binding.btnResetPassword.text = "Reset Password"

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset link sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to send reset email"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}

