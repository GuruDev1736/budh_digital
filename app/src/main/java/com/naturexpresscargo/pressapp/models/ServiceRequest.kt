package com.naturexpresscargo.pressapp.models

data class ServiceRequest(
    val requestId: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val name: String = "",
    val mobileNo: String = "",
    val email: String = "",
    val address: String = "",
    val additionalNote: String = "",
    val requiredDate: String = "",
    val status: String = "Pending",
    val creationDate: Long = System.currentTimeMillis()
)

