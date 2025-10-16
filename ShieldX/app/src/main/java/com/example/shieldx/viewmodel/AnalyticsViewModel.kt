package com.example.shieldx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.shieldx.models.*
import com.example.shieldx.data.AnalyticsData
import com.example.shieldx.repository.AnalyticsRepository
import com.example.shieldx.network.ApiClient
import kotlinx.coroutines.launch

/**
 * DeepGuard v3.0 - Analytics ViewModel
 * Handles analytics data and dashboard statistics
 */
class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val analyticsRepository = AnalyticsRepository(application, ApiClient.getApiService())
    
    // Dashboard state
    private val _dashboardState = MutableLiveData<DashboardState>()
    val dashboardState: LiveData<DashboardState> = _dashboardState
    
    // User statistics
    private val _userStats = MutableLiveData<UserStats?>()
    val userStats: LiveData<UserStats?> = _userStats
    
    // Daily statistics
    private val _dailyStats = MutableLiveData<List<DailyStat>>()
    val dailyStats: LiveData<List<DailyStat>> = _dailyStats
    
    // Weekly trends
    private val _weeklyTrends = MutableLiveData<List<WeeklyTrend>>()
    val weeklyTrends: LiveData<List<WeeklyTrend>> = _weeklyTrends
    
    // Scan summary
    private val _scanSummary = MutableLiveData<ScanSummary?>()
    val scanSummary: LiveData<ScanSummary?> = _scanSummary
    
    // Recent scans
    private val _recentScans = MutableLiveData<List<ScanResult>>()
    val recentScans: LiveData<List<ScanResult>> = _recentScans
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Analytics data for compatibility
    private val _analyticsData = MutableLiveData<com.example.shieldx.data.AnalyticsData?>()
    val analyticsData: LiveData<com.example.shieldx.data.AnalyticsData?> = _analyticsData
    
    // Recent detections for compatibility
    private val _recentDetections = MutableLiveData<List<Detection>>()
    val recentDetections: LiveData<List<Detection>> = _recentDetections
    
    // Error for compatibility
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        // Initialize with loading state
        _dashboardState.value = DashboardState(isLoading = true)
        
        // Load initial data
        loadDashboardData()
    }
    
    /**
     * Load complete dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _dashboardState.value = DashboardState(isLoading = true)
            
            try {
                // Load dashboard analytics
                val result = analyticsRepository.getDashboardAnalytics(7)
                
                result.fold(
                    onSuccess = { statsResponse ->
                        _userStats.value = statsResponse.userStats
                        _dailyStats.value = statsResponse.dailyStats
                        _weeklyTrends.value = statsResponse.weeklyTrend
                        _scanSummary.value = statsResponse.scanSummary
                        
                        _dashboardState.value = DashboardState(
                            isLoading = false,
                            userStats = statsResponse.userStats,
                            recentScans = emptyList(), // Will be loaded separately
                            error = null
                        )
                        
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _dashboardState.value = DashboardState(
                            isLoading = false,
                            error = exception.message ?: "Failed to load dashboard data"
                        )
                        _errorMessage.value = exception.message ?: "Failed to load dashboard data"
                    }
                )
            } catch (e: Exception) {
                _dashboardState.value = DashboardState(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load user statistics for specific period
     */
    fun loadUserStats(period: String = "week") {
        viewModelScope.launch {
            try {
                val result = analyticsRepository.getUserStats(period)
                
                result.fold(
                    onSuccess = { statsResponse ->
                        _userStats.value = statsResponse.userStats
                        _dailyStats.value = statsResponse.dailyStats
                        _weeklyTrends.value = statsResponse.weeklyTrend
                        _scanSummary.value = statsResponse.scanSummary
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to load user statistics"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load user statistics"
            }
        }
    }
    
    /**
     * Load trends data
     */
    fun loadTrends(period: String = "month") {
        viewModelScope.launch {
            try {
                val result = analyticsRepository.getTrends(period)
                
                result.fold(
                    onSuccess = { trends ->
                        _weeklyTrends.value = trends
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to load trends data"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load trends data"
            }
        }
    }
    
    /**
     * Load scan summary
     */
    fun loadScanSummary() {
        viewModelScope.launch {
            try {
                val result = analyticsRepository.getScanSummary()
                
                result.fold(
                    onSuccess = { summary ->
                        _scanSummary.value = summary
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to load scan summary"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load scan summary"
            }
        }
    }
    
    /**
     * Refresh all data
     */
    fun refreshData() {
        loadDashboardData()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = ""
    }
    
    /**
     * Get safety score color based on score value
     */
    fun getSafetyScoreColor(score: Double): Int {
        return when {
            score >= 80 -> android.graphics.Color.GREEN
            score >= 60 -> android.graphics.Color.YELLOW
            score >= 40 -> android.graphics.Color.parseColor("#FF8C00") // Dark Orange
            else -> android.graphics.Color.RED
        }
    }
    
    /**
     * Get risk level text based on score
     */
    fun getRiskLevelText(score: Double): String {
        return when {
            score >= 80 -> "Low Risk"
            score >= 60 -> "Medium Risk"
            score >= 40 -> "High Risk"
            else -> "Critical Risk"
        }
    }
    
    /**
     * Calculate total threats from weekly trends
     */
    fun getTotalThreats(): Int {
        return _weeklyTrends.value?.sumOf { it.totalThreats } ?: 0
    }
    
    /**
     * Get latest safety score from weekly trends
     */
    fun getLatestSafetyScore(): Double {
        return _weeklyTrends.value?.lastOrNull()?.riskLevel ?: 0.0
    }
    
    /**
     * Check if data is available
     */
    fun hasData(): Boolean {
        return _userStats.value != null
    }
    
    /**
     * Load analytics data for compatibility with AnalyticsActivity
     */
    fun loadAnalyticsData(timePeriod: String = "week") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Create mock analytics data - replace with actual API call
                val mockData = AnalyticsData(
                    totalScans = _userStats.value?.totalScans ?: 0,
                    threatsFound = getTotalThreats(),
                    safetyScore = getLatestSafetyScore(),
                    lastScan = "Today",
                    scanActivity = emptyList(),
                    threatTypes = mapOf("harassment" to 5, "deepfake" to 3, "spam" to 2),
                    detectionAccuracy = emptyList(),
                    recentDetections = emptyList()
                )
                _analyticsData.value = mockData
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load recent detections for compatibility
     */
    fun loadRecentDetections() {
        viewModelScope.launch {
            try {
                // Convert recent scans to detections
                val detections = _recentScans.value?.map { scan ->
                    Detection(
                        id = scan.id,
                        type = when {
                            scan.isHarassment -> "harassment"
                            scan.isDeepfake -> "deepfake"
                            scan.isHarmful -> "threat"
                            else -> "safe"
                        },
                        confidence = (scan.confidenceScore * 100).toInt(),
                        timestamp = System.currentTimeMillis(),
                        source = scan.fileName ?: "System",
                        details = scan.detailedAnalysis
                    )
                } ?: emptyList()
                _recentDetections.value = detections
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

/**
 * Dashboard State Data Class
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val userStats: UserStats? = null,
    val recentScans: List<ScanResult> = emptyList(),
    val error: String? = null
)
