package com.example.shieldx.data

data class NotificationPayload(
    val source: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val packageName: String? = null
)