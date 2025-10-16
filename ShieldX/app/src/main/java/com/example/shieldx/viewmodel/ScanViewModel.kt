package com.example.shieldx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.shieldx.models.*
import com.example.shieldx.repository.ScanRepository
import com.example.shieldx.network.ApiClient
import kotlinx.coroutines.launch

/**
 * DeepGuard v3.0 - Scan ViewModel
 * Handles all scanning operations and states
 */
class ScanViewModel(application: Application) : AndroidViewModel(application) {
    
    private val scanRepository = ScanRepository(application, ApiClient.getApiService())
    
    // Scan state
    private val _scanState = MutableLiveData<ScanState>()
    val scanState: LiveData<ScanState> = _scanState
    
    // Scan results
    private val _scanResults = MutableLiveData<List<ScanResult>>()
    val scanResults: LiveData<List<ScanResult>> = _scanResults
    
    // Current scan result
    private val _currentScanResult = MutableLiveData<ScanResult?>()
    val currentScanResult: LiveData<ScanResult?> = _currentScanResult
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Progress for file uploads
    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> = _uploadProgress
    
    // Error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Monitoring statistics
    private val _monitoringStats = MutableLiveData<MonitoringStats>()
    val monitoringStats: LiveData<MonitoringStats> = _monitoringStats
    
    // Recent alerts
    private val _recentAlerts = MutableLiveData<List<Alert>>()
    val recentAlerts: LiveData<List<Alert>> = _recentAlerts
    
    // Individual properties for backward compatibility with DeepfakeActivity
    private val _scanProgress = MutableLiveData<Int>()
    val scanProgress: LiveData<Int> = _scanProgress
    
    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> = _isScanning
    
    private val _scanError = MutableLiveData<String?>()
    val scanError: LiveData<String?> = _scanError
    
    private val _scanResult = MutableLiveData<ScanResult?>()
    val scanResult: LiveData<ScanResult?> = _scanResult
    
    init {
        // Initialize with empty state
        _scanState.value = ScanState()
        _scanResults.value = emptyList()
        
        // Load recent scan results
        loadUserScans()
    }
    
    /**
     * Scan text for harassment
     */
    fun scanText(text: String) {
        if (text.isBlank()) {
            _errorMessage.value = "Please enter text to scan"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _scanState.value = ScanState(isScanning = true, progress = 25)
            
            // Update individual properties
            _isScanning.value = true
            _scanProgress.value = 25
            _scanError.value = null
            
            try {
                val result = scanRepository.scanText(text)
                
                result.fold(
                    onSuccess = { scanResult ->
                        _currentScanResult.value = scanResult
                        _scanState.value = ScanState(
                            isScanning = false,
                            progress = 100,
                            result = scanResult,
                            isComplete = true
                        )
                        
                        // Update individual properties
                        _isScanning.value = false
                        _scanProgress.value = 100
                        _scanResult.value = scanResult
                        
                        // Refresh scan history
                        loadUserScans()
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _scanState.value = ScanState(
                            isScanning = false,
                            isError = true,
                            error = exception.message ?: "Text scan failed"
                        )
                        _errorMessage.value = exception.message ?: "Text scan failed"
                        
                        // Update individual properties
                        _isScanning.value = false
                        _scanError.value = exception.message ?: "Text scan failed"
                    }
                )
            } catch (e: Exception) {
                _scanState.value = ScanState(
                    isScanning = false,
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Scan media file
     */
    fun scanMedia(fileBytes: ByteArray, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _scanState.value = ScanState(isScanning = true, progress = 0)
            
            try {
                // Simulate upload progress
                updateProgress(25)
                
                val result = scanRepository.scanMedia(fileBytes, fileName, mimeType)
                
                updateProgress(75)
                
                result.fold(
                    onSuccess = { scanResult ->
                        updateProgress(100)
                        _currentScanResult.value = scanResult
                        _scanState.value = ScanState(
                            isScanning = false,
                            progress = 100,
                            result = scanResult,
                            isComplete = true
                        )
                        
                        // Refresh scan history
                        loadUserScans()
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _scanState.value = ScanState(
                            isScanning = false,
                            isError = true,
                            error = exception.message ?: "Media scan failed"
                        )
                        _errorMessage.value = exception.message ?: "Media scan failed"
                    }
                )
            } catch (e: Exception) {
                _scanState.value = ScanState(
                    isScanning = false,
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Scan for deepfakes
     */
    fun scanDeepfake(fileBytes: ByteArray, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _scanState.value = ScanState(isScanning = true, progress = 0)
            
            try {
                // Simulate upload progress
                updateProgress(30)
                
                val result = scanRepository.scanDeepfake(fileBytes, fileName, mimeType)
                
                updateProgress(80)
                
                result.fold(
                    onSuccess = { scanResult ->
                        updateProgress(100)
                        _currentScanResult.value = scanResult
                        _scanState.value = ScanState(
                            isScanning = false,
                            progress = 100,
                            result = scanResult,
                            isComplete = true
                        )
                        
                        // Refresh scan history
                        loadUserScans()
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _scanState.value = ScanState(
                            isScanning = false,
                            isError = true,
                            error = exception.message ?: "Deepfake scan failed"
                        )
                        _errorMessage.value = exception.message ?: "Deepfake scan failed"
                    }
                )
            } catch (e: Exception) {
                _scanState.value = ScanState(
                    isScanning = false,
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Start deep scan
     */
    fun startDeepScan() {
        viewModelScope.launch {
            _isLoading.value = true
            _scanState.value = ScanState(isScanning = true, progress = 0)
            
            try {
                // Simulate deep scan progress
                for (i in 1..10) {
                    updateProgress(i * 10)
                    kotlinx.coroutines.delay(500) // Simulate processing time
                }
                
                val result = scanRepository.startDeepScan()
                
                result.fold(
                    onSuccess = { scanResult ->
                        _currentScanResult.value = scanResult
                        _scanState.value = ScanState(
                            isScanning = false,
                            progress = 100,
                            result = scanResult,
                            isComplete = true
                        )
                        
                        // Refresh scan history
                        loadUserScans()
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _scanState.value = ScanState(
                            isScanning = false,
                            isError = true,
                            error = exception.message ?: "Deep scan failed"
                        )
                        _errorMessage.value = exception.message ?: "Deep scan failed"
                    }
                )
            } catch (e: Exception) {
                _scanState.value = ScanState(
                    isScanning = false,
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load user's scan history
     */
    fun loadUserScans(limit: Int = 20, offset: Int = 0) {
        viewModelScope.launch {
            try {
                val result = scanRepository.getUserScans(limit, offset)
                result.fold(
                    onSuccess = { scans ->
                        _scanResults.value = scans
                    },
                    onFailure = { exception ->
                        // Handle silently or show error
                        _errorMessage.value = exception.message ?: "Failed to load scan history"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load scan history"
            }
        }
    }
    
    /**
     * Delete a scan result
     */
    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            try {
                val result = scanRepository.deleteScan(scanId)
                result.fold(
                    onSuccess = { message ->
                        // Refresh scan history
                        loadUserScans()
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to delete scan"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete scan"
            }
        }
    }
    
    /**
     * Clear current scan result
     */
    fun clearCurrentScanResult() {
        _currentScanResult.value = null
        _scanState.value = ScanState()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = ""
    }
    
    /**
     * Reset scan state
     */
    fun resetScanState() {
        _scanState.value = ScanState()
        _currentScanResult.value = null
        _uploadProgress.value = 0
    }
    
    /**
     * Update progress
     */
    private fun updateProgress(progress: Int) {
        _uploadProgress.value = progress
        val currentState = _scanState.value ?: ScanState()
        _scanState.value = currentState.copy(progress = progress)
    }
    
    /**
     * Load monitoring statistics from shared preferences
     */
    fun loadMonitoringStats() {
        viewModelScope.launch {
            try {
                val sharedPref = com.example.shieldx.utils.SharedPref.getInstance(getApplication())
                val notificationsScanned = sharedPref.getIntValue("notifications_scanned", 0)
                val threatsBlocked = sharedPref.getIntValue("threats_blocked", 0)
                val warningsSent = sharedPref.getIntValue("warnings_sent", 0)
                
                val stats = MonitoringStats(
                    notificationsScanned = notificationsScanned,
                    threatsBlocked = threatsBlocked,
                    warningsSent = warningsSent
                )
                
                _monitoringStats.value = stats
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load monitoring stats: ${e.message}"
            }
        }
    }
    
    /**
     * Load recent alerts from repository
     */
    fun loadRecentAlerts() {
        viewModelScope.launch {
            try {
                // For now, load a sample list of alerts
                // In a real implementation, this would come from a database or API
                val sampleAlerts = listOf(
                    Alert(
                        id = "1",
                        title = "Harassment Detected",
                        message = "Harassment detected in WhatsApp message",
                        appName = "WhatsApp",
                        threatType = "harassment",
                        confidence = 85,
                        timestamp = System.currentTimeMillis(),
                        isBlocked = true
                    ),
                    Alert(
                        id = "2",
                        title = "Deepfake Alert", 
                        message = "Potential deepfake image detected",
                        appName = "Instagram",
                        threatType = "deepfake",
                        confidence = 75,
                        timestamp = System.currentTimeMillis() - 3600000,
                        isBlocked = false
                    )
                )
                
                _recentAlerts.value = sampleAlerts
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recent alerts: ${e.message}"
                _recentAlerts.value = emptyList()
            }
        }
    }
    
    /**
     * Scan file for deepfake detection
     */
    fun scanFile(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _scanState.value = ScanState(isScanning = true, progress = 0)
            
            // Update individual properties
            _isScanning.value = true
            _scanProgress.value = 0
            _scanError.value = null
            
            try {
                // Read file bytes from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (fileBytes == null) {
                    throw Exception("Failed to read file")
                }
                
                // Get file name and MIME type
                val fileName = uri.lastPathSegment ?: "unknown_file"
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                
                // Update progress
                _scanProgress.value = 25
                
                // Perform deepfake scan
                val result = scanRepository.scanDeepfake(fileBytes, fileName, mimeType)
                
                result.fold(
                    onSuccess = { scanResult ->
                        _currentScanResult.value = scanResult
                        _scanState.value = ScanState(
                            isScanning = false,
                            progress = 100,
                            result = scanResult,
                            isComplete = true
                        )
                        
                        // Update individual properties
                        _isScanning.value = false
                        _scanProgress.value = 100
                        _scanResult.value = scanResult
                        
                        // Refresh scan history
                        loadUserScans()
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _scanState.value = ScanState(
                            isScanning = false,
                            isError = true,
                            error = exception.message ?: "File scan failed"
                        )
                        _errorMessage.value = exception.message ?: "File scan failed"
                        
                        // Update individual properties
                        _isScanning.value = false
                        _scanError.value = exception.message ?: "File scan failed"
                    }
                )
            } catch (e: Exception) {
                _scanState.value = ScanState(
                    isScanning = false,
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                
                // Update individual properties
                _isScanning.value = false
                _scanError.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Scan State Data Class
 */
data class ScanState(
    val isScanning: Boolean = false,
    val progress: Int = 0,
    val result: ScanResult? = null,
    val isComplete: Boolean = false,
    val isError: Boolean = false,
    val error: String = ""
)
