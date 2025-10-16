package com.example.shieldx.data

data class AnalyticsData(
    val totalScans: Int,
    val threatsFound: Int,
    val safetyScore: Double,
    val lastScan: String,
    val scanActivity: List<ScanActivityData>,
    val threatTypes: Map<String, Int>,
    val detectionAccuracy: List<AccuracyData>,
    val recentDetections: List<DetectionData>
)

data class ScanActivityData(
    val date: String,
    val scans: Int
)

data class AccuracyData(
    val date: String,
    val accuracy: Double
)

data class DetectionData(
    val type: String,
    val source: String,
    val time: String,
    val confidence: Double
)