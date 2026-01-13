package com.budhdigital.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.budhdigital.app.databinding.ActivityContactUsBinding
import com.budhdigital.app.models.ContactMessage

class ContactUsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactUsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupEdgeToEdge()
        setupToolbar()
        setupValidation()
        loadUserData()
        setupListeners()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.setPadding(
                0,
                insets.top,
                0,
                0
            )

            view.setPadding(
                0,
                0,
                0,
                insets.bottom
            )

            windowInsets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: currentUser.email ?: ""
                    val phone = snapshot.child("phone").getValue(String::class.java) ?: ""

                    binding.etFullName.setText(fullName)
                    binding.etEmail.setText(email)
                    if (phone.isNotEmpty()) {
                        binding.etPhoneNumber.setText(phone)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSendMessage.setOnClickListener {
            submitContactForm()
        }
    }

    private fun setupValidation() {
        // Full Name validation
        binding.etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                when {
                    name.isEmpty() -> {
                        binding.tilFullName.error = null
                    }
                    name.length < 3 -> {
                        binding.tilFullName.error = "Name must be at least 3 characters"
                    }
                    !name.matches(Regex("^[a-zA-Z\\s]+$")) -> {
                        binding.tilFullName.error = "Name should contain only letters"
                    }
                    else -> {
                        binding.tilFullName.error = null
                    }
                }
            }
        })

        // Phone Number validation
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                when {
                    phone.isEmpty() -> {
                        binding.tilPhoneNumber.error = null
                    }
                    !phone.matches(Regex("^[0-9+\\-\\s()]+$")) -> {
                        binding.tilPhoneNumber.error = "Invalid phone number format"
                    }
                    phone.replace(Regex("[^0-9]"), "").length < 10 -> {
                        binding.tilPhoneNumber.error = "Phone number must be at least 10 digits"
                    }
                    phone.replace(Regex("[^0-9]"), "").length > 15 -> {
                        binding.tilPhoneNumber.error = "Phone number is too long"
                    }
                    else -> {
                        binding.tilPhoneNumber.error = null
                    }
                }
            }
        })

        // Email validation
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                when {
                    email.isEmpty() -> {
                        binding.tilEmail.error = null
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        binding.tilEmail.error = "Invalid email address"
                    }
                    else -> {
                        binding.tilEmail.error = null
                    }
                }
            }
        })

        // Subject validation
        binding.etSubject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val subject = s.toString().trim()
                when {
                    subject.isEmpty() -> {
                        binding.tilSubject.error = null
                    }
                    subject.length < 5 -> {
                        binding.tilSubject.error = "Subject must be at least 5 characters"
                    }
                    subject.length > 100 -> {
                        binding.tilSubject.error = "Subject is too long (max 100 characters)"
                    }
                    else -> {
                        binding.tilSubject.error = null
                    }
                }
            }
        })

        // Questions/Message validation
        binding.etQuestions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val message = s.toString().trim()
                when {
                    message.isEmpty() -> {
                        binding.tilQuestions.error = null
                    }
                    message.length < 10 -> {
                        binding.tilQuestions.error = "Message must be at least 10 characters"
                    }
                    message.length > 1000 -> {
                        binding.tilQuestions.error = "Message is too long (max 1000 characters)"
                    }
                    else -> {
                        binding.tilQuestions.error = null
                    }
                }
            }
        })
    }

    private fun submitContactForm() {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val subject = binding.etSubject.text.toString().trim()
        val message = binding.etQuestions.text.toString().trim()

        // Clear previous errors
        binding.tilFullName.error = null
        binding.tilPhoneNumber.error = null
        binding.tilEmail.error = null
        binding.tilSubject.error = null
        binding.tilQuestions.error = null

        // Comprehensive validation
        var isValid = true
        var firstErrorField: View? = null

        // Validate Full Name
        when {
            fullName.isEmpty() -> {
                binding.tilFullName.error = "Full name is required"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etFullName
            }
            fullName.length < 3 -> {
                binding.tilFullName.error = "Name must be at least 3 characters"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etFullName
            }
            !fullName.matches(Regex("^[a-zA-Z\\s]+$")) -> {
                binding.tilFullName.error = "Name should contain only letters"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etFullName
            }
        }

        // Validate Phone Number
        val phoneDigits = phoneNumber.replace(Regex("[^0-9]"), "")
        when {
            phoneNumber.isEmpty() -> {
                binding.tilPhoneNumber.error = "Phone number is required"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etPhoneNumber
            }
            !phoneNumber.matches(Regex("^[0-9+\\-\\s()]+$")) -> {
                binding.tilPhoneNumber.error = "Invalid phone number format"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etPhoneNumber
            }
            phoneDigits.length < 10 -> {
                binding.tilPhoneNumber.error = "Phone number must be at least 10 digits"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etPhoneNumber
            }
            phoneDigits.length > 15 -> {
                binding.tilPhoneNumber.error = "Phone number is too long"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etPhoneNumber
            }
        }

        // Validate Email
        when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email address is required"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etEmail
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email address"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etEmail
            }
        }

        // Validate Subject
        when {
            subject.isEmpty() -> {
                binding.tilSubject.error = "Subject is required"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etSubject
            }
            subject.length < 5 -> {
                binding.tilSubject.error = "Subject must be at least 5 characters"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etSubject
            }
            subject.length > 100 -> {
                binding.tilSubject.error = "Subject is too long (max 100 characters)"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etSubject
            }
        }

        // Validate Message
        when {
            message.isEmpty() -> {
                binding.tilQuestions.error = "Message is required"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etQuestions
            }
            message.length < 10 -> {
                binding.tilQuestions.error = "Message must be at least 10 characters"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etQuestions
            }
            message.length > 1000 -> {
                binding.tilQuestions.error = "Message is too long (max 1000 characters)"
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.etQuestions
            }
        }

        if (!isValid) {
            // Focus on the first field with error
            firstErrorField?.requestFocus()
            Toast.makeText(this, "Please fix the errors before submitting", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress and disable button
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendMessage.isEnabled = false
        binding.btnSendMessage.text = "Sending..."

        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "guest"

        // Generate unique ID for the message
        val messageId = database.reference.child("contact_messages").push().key ?: return

        val contactMessage = ContactMessage(
            id = messageId,
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            subject = subject,
            message = message,
            timestamp = System.currentTimeMillis(),
            userId = userId,
            status = "Pending"
        )

        // Save to Firebase
        database.reference.child("contact_messages").child(messageId)
            .setValue(contactMessage)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSendMessage.isEnabled = true
                binding.btnSendMessage.text = "Send Message"

                Toast.makeText(
                    this,
                    "Message sent successfully! We'll get back to you soon.",
                    Toast.LENGTH_LONG
                ).show()

                // Clear form
                binding.etSubject.setText("")
                binding.etQuestions.setText("")

                // Close activity after a short delay
                binding.root.postDelayed({
                    finish()
                }, 1500)
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.btnSendMessage.isEnabled = true
                binding.btnSendMessage.text = "Send Message"

                Toast.makeText(
                    this,
                    "Failed to send message: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

