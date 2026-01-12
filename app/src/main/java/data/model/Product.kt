package com.example.hibe7.data.model

import com.google.firebase.firestore.PropertyName

data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "", // Ana resim
    val images: List<String> = emptyList(), // Diğer resimler
    val ownerId: String = "",
    val timestamp: Long = 0,

    // BU KISIM ÇOK KRİTİK:
    // Kodda "isAvailable" kullan, Veritabanında "available" olarak kaydet/oku.
    @get:PropertyName("available")
    @set:PropertyName("available")
    var isAvailable: Boolean = true
)