package com.naturexpresscargo.pressapp.models

data class HistoryItem(
    val id: String,
    val title: String,
    val status: String,
    val statusColor: Int,
    val date: String,
    val icon: Int,
    val iconColor: Int,
    val actionButton: String,
    val creationDate: Long = 0L
)

