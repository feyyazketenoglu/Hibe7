package com.example.hibe7.data.model

data class Demand(
    val id: String = "",
    val productId: String = "",
    val requesterId: String = "",
    val ownerId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val requesterName: String = "",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)