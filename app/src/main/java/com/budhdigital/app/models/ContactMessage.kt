package com.budhdigital.app.models

data class ContactMessage(
    val id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val userId: String = "",
    val status: String = "Pending" // Pending, Read, Replied
)

