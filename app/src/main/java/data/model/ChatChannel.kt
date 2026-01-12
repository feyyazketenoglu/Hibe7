package com.example.hibe7.data.model

data class ChatChannel(
    val id: String = "",
    val userIds: List<String> = emptyList(), // Konuşanların ID listesi
    val lastMessage: String = "",            // Listede görünecek son mesaj
    val timestamp: Long = 0                  // Sıralama için zaman
)