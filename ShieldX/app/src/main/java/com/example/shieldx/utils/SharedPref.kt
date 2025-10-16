package com.example.shieldx.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.shieldx.models.User
import com.example.shieldx.models.UserSettings
import com.google.gson.Gson

/**
 * DeepGuard v3.0 - Secure Preferences Manager
 * Handles encrypted storage of sensitive data including JWT tokens
 */
class SharedPref private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SharedPref? = null
        
        fun getInstance(context: Context): SharedPref {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedPref(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Preference Keys
        private const val PREF_NAME = "deepguard_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_USER_SETTINGS = "user_settings"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_REAL_TIME_MODE = "real_time_mode"
        private const val KEY_AUTO_BLOCK = "auto_block"
        private const val KEY_PERMISSION_SETUP = "permission_setup_completed"
        private const val KEY_ADVANCED_ANALYSIS = "advanced_analysis"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    private val gson = Gson()
    
    // ================================
    // Authentication Methods
    // ================================
    
    fun saveAuthTokens(accessToken: String, refreshToken: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun isLoggedIn(): Boolean {
        // For bypass mode, only check the logged in flag, not the token
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            // If setting to logged in, create a dummy token
            if (isLoggedIn) {
                putString(KEY_ACCESS_TOKEN, "bypass-login-token")
            }
            apply()
        }
    }
    
    fun logout() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_DATA)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    // ================================
    // User Data Methods
    // ================================
    
    fun saveUserData(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_USER_DATA, userJson).apply()
    }
    
    fun getUserData(): User? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    // ================================
    // Settings Methods
    // ================================
    
    fun saveUserSettings(settings: UserSettings) {
        val settingsJson = gson.toJson(settings)
        sharedPreferences.edit().putString(KEY_USER_SETTINGS, settingsJson).apply()
    }
    
    fun getUserSettings(): UserSettings {
        val settingsJson = sharedPreferences.getString(KEY_USER_SETTINGS, null)
        return if (settingsJson != null) {
            try {
                gson.fromJson(settingsJson, UserSettings::class.java)
            } catch (e: Exception) {
                UserSettings() // Return default settings
            }
        } else {
            UserSettings() // Return default settings
        }
    }
    
    // ================================
    // App State Methods
    // ================================
    
    fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }
    
    fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }
    
    fun setLastSyncTime(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }
    
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0)
    }
    
    fun isFirstLaunch(): Boolean {
        val isFirst = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirst) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirst
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    // ================================
    // Utility Methods
    // ================================
    
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun saveStringValue(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    fun getStringValue(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    fun saveBooleanValue(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    fun getBooleanValue(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    fun saveIntValue(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    fun getIntValue(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    fun saveFloatValue(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }
    
    fun getFloatValue(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }
    
    // Detection Settings Methods
    fun setHarassmentDetectionEnabled(enabled: Boolean) = saveBooleanValue("harassment_detection", enabled)
    fun isHarassmentDetectionEnabled() = getBooleanValue("harassment_detection", true)
    
    fun setDeepfakeDetectionEnabled(enabled: Boolean) = saveBooleanValue("deepfake_detection", enabled)
    fun isDeepfakeDetectionEnabled() = getBooleanValue("deepfake_detection", true)
    
    // Agent Mode Settings
    fun setAgentModeEnabled(enabled: Boolean) = saveBooleanValue("agent_mode", enabled)
    fun isAgentModeEnabled() = getBooleanValue("agent_mode", false)
    
    fun setAgentPollingInterval(seconds: Int) = saveIntValue("agent_polling_interval", seconds)
    fun getAgentPollingInterval() = getIntValue("agent_polling_interval", 5)
    
    fun setAgentConfidenceThreshold(threshold: Float) = saveFloatValue("agent_confidence_threshold", threshold)
    fun getAgentConfidenceThreshold() = getFloatValue("agent_confidence_threshold", 0.8f)
    
    fun setAgentMaxBatchSize(size: Int) = saveIntValue("agent_max_batch_size", size)
    fun getAgentMaxBatchSize() = getIntValue("agent_max_batch_size", 10)
    // ================================
    // Monitoring Settings Methods
    // ================================
    
    fun setMonitoringActive(active: Boolean) = saveBooleanValue("monitoring_active", active)
    fun isMonitoringActive() = getBooleanValue("monitoring_active", false)
    
    fun setMonitoringStartTime(time: Long) = sharedPreferences.edit().putLong("monitoring_start_time", time).apply()
    fun getMonitoringStartTime() = sharedPreferences.getLong("monitoring_start_time", 0L)
    
    // ================================
    // Real-time Monitoring Methods
    // ================================
    
    fun setRealTimeModeEnabled(enabled: Boolean) = saveBooleanValue(KEY_REAL_TIME_MODE, enabled)
    fun isRealTimeModeEnabled() = getBooleanValue(KEY_REAL_TIME_MODE, false)
    
    fun setAutoBlockEnabled(enabled: Boolean) = saveBooleanValue(KEY_AUTO_BLOCK, enabled)
    fun isAutoBlockEnabled() = getBooleanValue(KEY_AUTO_BLOCK, false)
    
    // ================================
    // Notification Settings Methods
    // ================================
    
    fun setPushNotificationsEnabled(enabled: Boolean) = saveBooleanValue("push_notifications", enabled)
    fun isPushNotificationsEnabled() = getBooleanValue("push_notifications", true)
    
    fun setSoundAlertsEnabled(enabled: Boolean) = saveBooleanValue("sound_alerts", enabled)
    fun isSoundAlertsEnabled() = getBooleanValue("sound_alerts", true)
    
    fun setVibrationEnabled(enabled: Boolean) = saveBooleanValue("vibration", enabled)
    fun isVibrationEnabled() = getBooleanValue("vibration", true)
    
    // ================================
    // Advanced Settings Methods
    // ================================
    
    fun setAnalyticsEnabled(enabled: Boolean) = saveBooleanValue("analytics", enabled)
    fun isAnalyticsEnabled() = getBooleanValue("analytics", true)
    
    fun setAutoStartMonitoring(enabled: Boolean) = saveBooleanValue("auto_start_monitoring", enabled)
    fun isAutoStartMonitoringEnabled() = getBooleanValue("auto_start_monitoring", false)
    
    fun setNotificationThreshold(threshold: Int) = saveIntValue("notification_threshold", threshold)
    fun getNotificationThreshold() = getIntValue("notification_threshold", 50)
    
    fun setTrustedContacts(contacts: String) = saveStringValue("trusted_contacts", contacts)
    fun getTrustedContacts() = getStringValue("trusted_contacts", "")
    
    fun setMonitoredApps(apps: String) = saveStringValue("monitored_apps", apps)
    fun getMonitoredApps() = getStringValue("monitored_apps", "")
    
    fun setBlockedKeywords(keywords: String) = saveStringValue("blocked_keywords", keywords)
    fun getBlockedKeywords() = getStringValue("blocked_keywords", "")
    
    // ================================
    // Quiet Hours Methods
    // ================================
    
    fun setQuietHoursEnabled(enabled: Boolean) = saveBooleanValue("quiet_hours_enabled", enabled)
    fun isQuietHoursEnabled() = getBooleanValue("quiet_hours_enabled", false)
    
    fun setQuietHoursStart(hour: Int) = saveIntValue("quiet_hours_start", hour)
    fun getQuietHoursStart() = getIntValue("quiet_hours_start", 22)
    
    fun setQuietHoursEnd(hour: Int) = saveIntValue("quiet_hours_end", hour)
    fun getQuietHoursEnd() = getIntValue("quiet_hours_end", 7)

    // ================================
    // Permission Setup Methods
    // ================================
    
    /**
     * Sets whether the permission setup process has been completed
     */
    fun setPermissionSetupCompleted(completed: Boolean) = saveBooleanValue(KEY_PERMISSION_SETUP, completed)

    /**
     * Checks if the permission setup process has been completed
     */
    fun isPermissionSetupCompleted() = getBooleanValue(KEY_PERMISSION_SETUP, false)

    // ================================
    // Analysis Settings Methods
    // ================================
    
    /**
     * Sets whether advanced analysis is enabled
     */
    fun setAdvancedAnalysis(enabled: Boolean) = saveBooleanValue(KEY_ADVANCED_ANALYSIS, enabled)

    /**
     * Gets whether advanced analysis is enabled
     */
    fun getAdvancedAnalysis() = getBooleanValue(KEY_ADVANCED_ANALYSIS, true)
}
