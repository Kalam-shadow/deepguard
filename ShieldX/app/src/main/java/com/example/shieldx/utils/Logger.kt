package com.example.shieldx.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val TAG = "ShieldX"
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    fun d(tag: String, message: String) {
        Log.d("$TAG:$tag", formatMessage(message))
    }
    
    fun i(tag: String, message: String) {
        Log.i("$TAG:$tag", formatMessage(message))
    }
    
    fun w(tag: String, message: String) {
        Log.w("$TAG:$tag", formatMessage(message))
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG:$tag", formatMessage(message), throwable)
    }
    
    private fun formatMessage(message: String): String {
        val timestamp = dateFormat.format(Date())
        return "[$timestamp] $message"
    }
    
    // Specialized logging for harassment detection
    fun logHarassmentDetection(
        source: String,
        content: String,
        isHarassment: Boolean,
        confidence: Double,
        type: String?
    ) {
        val truncatedContent = content.take(100)
        val confidencePercent = (confidence * 100).toInt()
        
        val logMessage = """
            HARASSMENT DETECTION:
            Source: $source
            Content: "$truncatedContent${if (content.length > 100) "..." else ""}"
            Is Harassment: $isHarassment
            Confidence: $confidencePercent%
            Type: ${type ?: "Unknown"}
        """.trimIndent()
        
        if (isHarassment) {
            w("Detection", logMessage)
        } else {
            d("Detection", logMessage)
        }
    }
    
    fun logApiCall(endpoint: String, success: Boolean, responseCode: Int, duration: Long) {
        val message = """
            API CALL:
            Endpoint: $endpoint
            Success: $success
            Response Code: $responseCode
            Duration: ${duration}ms
        """.trimIndent()
        
        if (success) {
            d("API", message)
        } else {
            w("API", message)
        }
    }
}
