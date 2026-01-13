package com.budhdigital.app

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
import com.google.firebase.database.FirebaseDatabase
import com.budhdigital.app.databinding.ActivitySignupBinding
import com.budhdigital.app.models.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

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
        binding.ivPhoneEmailCheck.setImageResource(R.drawable.ic_email)
        binding.ivPhoneEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))

        binding.etPhoneEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && binding.etPhoneEmail.hasFocus()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        binding.ivPhoneEmailCheck.setImageResource(R.drawable.ic_check)
                        binding.ivPhoneEmailCheck.setColorFilter(ContextCompat.getColor(this@SignUpActivity, R.color.status_completed))
                    } else {
                        binding.ivPhoneEmailCheck.setImageResource(R.drawable.ic_error)
                        binding.ivPhoneEmailCheck.clearColorFilter()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPhoneEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.ivPhoneEmailCheck.setImageResource(R.drawable.ic_email)
                binding.ivPhoneEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))
            } else {
                val email = binding.etPhoneEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        binding.ivPhoneEmailCheck.setImageResource(R.drawable.ic_check)
                        binding.ivPhoneEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.status_completed))
                    } else {
                        binding.ivPhoneEmailCheck.setImageResource(R.drawable.ic_error)
                        binding.ivPhoneEmailCheck.clearColorFilter()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivPasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.ivConfirmPasswordVisibility.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        binding.btnSignUp.setOnClickListener {
            handleSignUp()
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun handleSignUp() {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneEmail = binding.etPhoneEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneEmail.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(phoneEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
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

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSignUp.isEnabled = false
        binding.btnSignUp.text = "Creating account..."

        auth.createUserWithEmailAndPassword(phoneEmail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserToDatabase(it.uid, fullName, phoneEmail)
                    }
                } else {
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "Sign Up"
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToDatabase(uid: String, fullName: String, email: String) {
        val user = User(
            uid = uid,
            fullName = fullName,
            email = email,
            phoneNumber = "",
            createdAt = System.currentTimeMillis()
        )

        database.reference.child("users").child(uid).setValue(user)
            .addOnCompleteListener { task ->
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "Sign Up"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

