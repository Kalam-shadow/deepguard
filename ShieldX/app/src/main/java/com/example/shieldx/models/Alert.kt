package com.example.shieldx.models

data class Alert(
    val id: String,
    val title: String,
    val message: String,
    val appName: String,
    val threatType: String,
    val confidence: Int,
    val timestamp: Long,
    val content: String? = null,
    val isBlocked: Boolean = false
)