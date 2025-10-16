package com.example.shieldx.data

data class AnalysisResponse(
    val harassment: HarassmentResult,
    val deepfake: DeepfakeResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class HarassmentResult(
    val isHarassment: Boolean,
    val confidence: Double,
    val category: String? = null,
    val details: String? = null,
    val type: String? = null,
    val severity: String? = null
)

data class DeepfakeResult(
    val isDeepfake: Boolean,
    val confidence: Double,
    val details: String? = null
)