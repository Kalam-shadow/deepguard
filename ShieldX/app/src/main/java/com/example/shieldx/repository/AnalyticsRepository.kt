package com.example.shieldx.repository

import android.content.Context
import android.util.Log
import com.example.shieldx.models.*
import com.example.shieldx.network.ApiService
import com.example.shieldx.network.NetworkUtils
import com.example.shieldx.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DeepGuard v3.0 - Analytics Repository
 * Handles all analytics and statistics API calls
 */
class AnalyticsRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val sharedPref = SharedPref.getInstance(context)
    private val tag = "AnalyticsRepository"
    
    /**
     * Get user statistics
     */
    suspend fun getUserStats(period: String = "week"): Result<StatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.getUserStats(NetworkUtils.createAuthHeader(token), period)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(tag, "User stats retrieved for period: $period")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to get user stats"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to get user stats: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Get user stats error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get dashboard analytics
     */
    suspend fun getDashboardAnalytics(days: Int = 7): Result<StatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.getDashboardAnalytics(NetworkUtils.createAuthHeader(token), days)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(tag, "Dashboard analytics retrieved for $days days")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to get dashboard analytics"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to get dashboard analytics: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Get dashboard analytics error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get trends data
     */
    suspend fun getTrends(period: String = "month"): Result<List<WeeklyTrend>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.getTrends(NetworkUtils.createAuthHeader(token), period)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(tag, "Trends data retrieved for period: $period")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to get trends"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to get trends: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Get trends error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get scan summary
     */
    suspend fun getScanSummary(): Result<ScanSummary> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.getScanSummary(NetworkUtils.createAuthHeader(token))
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(tag, "Scan summary retrieved")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to get scan summary"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to get scan summary: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Get scan summary error", e)
                Result.failure(e)
            }
        }
    }
    
    private fun getAuthToken(): String {
        val token = sharedPref.getAccessToken()
        // Use bypass token if no real token is available
        return if (token.isNullOrEmpty()) {
            Log.d(tag, "No auth token, using bypass-login-token")
            "bypass-login-token"
        } else {
            token
        }
    }
}

/**
 * DeepGuard v3.0 - File Repository
 * Handles file upload/download operations
 */
class FileRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val sharedPref = SharedPref.getInstance(context)
    private val tag = "FileRepository"
    
    /**
     * Upload file
     */
    suspend fun uploadFile(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        fileType: String
    ): Result<FileUploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val filePart = NetworkUtils.createMultipartFile(fileBytes, fileName, mimeType)
                val fileTypePart = NetworkUtils.createRequestBody(fileType)
                
                val response = apiService.uploadFile(
                    NetworkUtils.createAuthHeader(token),
                    filePart,
                    fileTypePart
                )
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(tag, "File uploaded successfully: $fileName")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "File upload failed"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "File upload failed: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Upload file error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Download file
     */
    suspend fun downloadFile(fileId: String): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.downloadFile(NetworkUtils.createAuthHeader(token), fileId)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val bytes = body.bytes()
                        Log.d(tag, "File downloaded successfully: $fileId")
                        Result.success(bytes)
                    } else {
                        val error = "No file data received"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "File download failed: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Download file error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Delete file
     */
    suspend fun deleteFile(fileId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.deleteFile(NetworkUtils.createAuthHeader(token), fileId)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d(tag, "File deleted successfully: $fileId")
                        Result.success("File deleted successfully")
                    } else {
                        val error = apiResponse?.error ?: "File deletion failed"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "File deletion failed: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Delete file error", e)
                Result.failure(e)
            }
        }
    }
    
    private fun getAuthToken(): String {
        val token = sharedPref.getAccessToken()
        // Use bypass token if no real token is available
        return if (token.isNullOrEmpty()) {
            Log.d(tag, "No auth token, using bypass-login-token")
            "bypass-login-token"
        } else {
            token
        }
    }
}
