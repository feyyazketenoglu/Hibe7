package com.example.hibe7.data.model

import com.google.firebase.firestore.PropertyName

data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val ownerId: String = "",
    val timestamp: Long = 0,

    @get:PropertyName("available")
    @set:PropertyName("available")
    var isAvailable: Boolean = true
)