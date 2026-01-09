package com.naturexpresscargo.pressapp

import android.app.DatePickerDialog
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
import com.google.firebase.database.FirebaseDatabase
import com.naturexpresscargo.pressapp.databinding.ActivityServiceRequestBinding
import com.naturexpresscargo.pressapp.models.ServiceRequest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class ServiceRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceRequestBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var serviceId: String = ""
    private var serviceName: String = ""
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityServiceRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""

        setupEdgeToEdge()
        setupToolbar()
        setupValidation()
        setupListeners()
        loadUserData()
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
        supportActionBar?.title = serviceName
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupValidation() {
        binding.ivNameCheck.setImageResource(R.drawable.ic_profile)
        binding.ivNameCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))

        binding.ivMobileCheck.setImageResource(R.drawable.ic_profile)
        binding.ivMobileCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))

        binding.ivEmailCheck.setImageResource(R.drawable.ic_email)
        binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))

        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s.toString().trim()
                if (name.isNotEmpty() && binding.etName.hasFocus()) {
                    if (name.length >= 3) {
                        binding.ivNameCheck.setImageResource(R.drawable.ic_check)
                        binding.ivNameCheck.setColorFilter(ContextCompat.getColor(this@ServiceRequestActivity, R.color.status_completed))
                    } else {
                        binding.ivNameCheck.setImageResource(R.drawable.ic_error)
                        binding.ivNameCheck.clearColorFilter()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.ivNameCheck.setImageResource(R.drawable.ic_profile)
                binding.ivNameCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))
            }
        }

        binding.etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val mobile = s.toString().trim()
                if (mobile.isNotEmpty() && binding.etMobile.hasFocus()) {
                    if (mobile.length == 10 && mobile.all { it.isDigit() }) {
                        binding.ivMobileCheck.setImageResource(R.drawable.ic_check)
                        binding.ivMobileCheck.setColorFilter(ContextCompat.getColor(this@ServiceRequestActivity, R.color.status_completed))
                    } else {
                        binding.ivMobileCheck.setImageResource(R.drawable.ic_error)
                        binding.ivMobileCheck.clearColorFilter()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etMobile.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.ivMobileCheck.setImageResource(R.drawable.ic_profile)
                binding.ivMobileCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))
            }
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && binding.etEmail.hasFocus()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        binding.ivEmailCheck.setImageResource(R.drawable.ic_check)
                        binding.ivEmailCheck.setColorFilter(ContextCompat.getColor(this@ServiceRequestActivity, R.color.status_completed))
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
            }
        }
    }

    private fun setupListeners() {
        binding.etRequiredDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            submitRequest()
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

                    binding.etName.setText(fullName)
                    binding.etEmail.setText(email)
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                selectedDate = dateFormat.format(selectedCalendar.time)
                binding.etRequiredDate.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun submitRequest() {
        val name = binding.etName.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val additionalNote = binding.etAdditionalNote.text.toString().trim()
        val requiredDate = binding.etRequiredDate.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            binding.etName.requestFocus()
            return
        }

        if (name.length < 3) {
            Toast.makeText(this, "Name must be at least 3 characters", Toast.LENGTH_SHORT).show()
            binding.etName.requestFocus()
            return
        }

        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
            binding.etMobile.requestFocus()
            return
        }

        if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
            Toast.makeText(this, "Please enter valid 10 digit mobile number", Toast.LENGTH_SHORT).show()
            binding.etMobile.requestFocus()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            binding.etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter valid email address", Toast.LENGTH_SHORT).show()
            binding.etEmail.requestFocus()
            return
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show()
            binding.etAddress.requestFocus()
            return
        }

        if (requiredDate.isEmpty()) {
            Toast.makeText(this, "Please select required date", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Submitting..."

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val requestId = generateRequestId()

            val serviceRequest = ServiceRequest(
                requestId = requestId,
                userId = userId,
                serviceId = serviceId,
                serviceName = serviceName,
                name = name,
                mobileNo = mobile,
                email = email,
                address = address,
                additionalNote = additionalNote,
                requiredDate = requiredDate,
                status = "Pending",
                creationDate = System.currentTimeMillis()
            )

            database.reference.child("requests").child(requestId).setValue(serviceRequest)
                .addOnCompleteListener { task ->
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Submit Request"

                    if (task.isSuccessful) {
                        Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to submit request: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun generateRequestId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val idLength = Random.nextInt(8, 11)
        val randomPart = (1..idLength)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        return "REQ-$randomPart"
    }
}

