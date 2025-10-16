package com.example.shieldx.models

data class Detection(
    val id: String,
    val type: String,
    val source: String,
    val confidence: Int,
    val timestamp: Long,
    val details: String? = null
)