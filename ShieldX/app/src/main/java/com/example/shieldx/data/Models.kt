package com.example.shieldx.data

import com.google.gson.annotations.SerializedName

data class NotificationPayload(
    @SerializedName("content")
    val content: String,
    
    @SerializedName("source")
    val source: String,
    
    @SerializedName("sender")
    val sender: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("app_name")
    val appName: String? = null,
    
    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
)

data class AnalysisResponse(
    @SerializedName("harassment")
    val harassment: HarassmentAnalysis,
    
    @SerializedName("analysis_id")
    val analysisId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null,
    
    @SerializedName("processing_time_ms")
    val processingTimeMs: Long? = null
)

data class HarassmentAnalysis(
    @SerializedName("is_harassment")
    val isHarassment: Boolean,
    
    @SerializedName("confidence")
    val confidence: Double,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("severity")
    val severity: String? = null,
    
    @SerializedName("keywords_detected")
    val keywordsDetected: List<String>? = null,
    
    @SerializedName("explanation")
    val explanation: String? = null
)