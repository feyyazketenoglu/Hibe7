package com.example.hibe7.data.model

data class Message(
    val id: String = "",
    val senderId: String = "", // Mesajı gönderen
    val text: String = "",     // Mesaj içeriği
    val timestamp: Long = 0    // Gönderilme zamanı
)