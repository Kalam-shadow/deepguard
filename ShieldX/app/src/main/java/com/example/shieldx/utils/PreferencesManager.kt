package com.example.shieldx.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "shieldx_prefs"
        
        // Keys
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_API_ENDPOINT = "api_endpoint"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DETECTION_SENSITIVITY = "detection_sensitivity"
        private const val KEY_LAST_BACKEND_TEST = "last_backend_test"
        private const val KEY_TOTAL_NOTIFICATIONS_PROCESSED = "total_notifications_processed"
        private const val KEY_TOTAL_HARASSMENT_DETECTED = "total_harassment_detected"
    }
    
    // First launch flag
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    // Notifications enabled
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    // API configuration
    var apiEndpoint: String?
        get() = prefs.getString(KEY_API_ENDPOINT, null)
        set(value) = prefs.edit().putString(KEY_API_ENDPOINT, value).apply()
    
    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()
    
    // Detection sensitivity (0.0 to 1.0)
    var detectionSensitivity: Float
        get() = prefs.getFloat(KEY_DETECTION_SENSITIVITY, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_DETECTION_SENSITIVITY, value).apply()
    
    // Backend test timestamp
    var lastBackendTest: Long
        get() = prefs.getLong(KEY_LAST_BACKEND_TEST, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_BACKEND_TEST, value).apply()
    
    // Statistics
    var totalNotificationsProcessed: Int
        get() = prefs.getInt(KEY_TOTAL_NOTIFICATIONS_PROCESSED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_NOTIFICATIONS_PROCESSED, value).apply()
    
    var totalHarassmentDetected: Int
        get() = prefs.getInt(KEY_TOTAL_HARASSMENT_DETECTED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_HARASSMENT_DETECTED, value).apply()
    
    // Helper methods
    fun incrementNotificationsProcessed() {
        totalNotificationsProcessed++
    }
    
    fun incrementHarassmentDetected() {
        totalHarassmentDetected++
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    fun getDetectionThreshold(): Double = detectionSensitivity.toDouble()
}
