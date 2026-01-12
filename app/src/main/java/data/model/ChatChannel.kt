package com.example.hibe7.data.model

data class ChatChannel(
    val id: String = "",
    val userIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val timestamp: Long = 0
)