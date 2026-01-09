package com.naturexpresscargo.pressapp.models
data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
