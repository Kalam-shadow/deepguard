package com.example.shieldx.repository

import android.content.Context
import android.util.Log
import com.example.shieldx.models.*
import com.example.shieldx.network.ApiService
import com.example.shieldx.network.NetworkUtils
import com.example.shieldx.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

/**
 * DeepGuard v3.0 - Authentication Repository
 * Handles all authentication-related API calls and token management
 */
class AuthRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val sharedPref = SharedPref.getInstance(context)
    private val tag = "AuthRepository"
    
    /**
     * User login
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(username, password)
                val response = apiService.login(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Save tokens and user data
                        sharedPref.saveAuthTokens(apiResponse.data.accessToken)
                        sharedPref.saveUserData(apiResponse.data.user)
                        
                        Log.d(tag, "Login successful for user: ${apiResponse.data.user.username}")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Login failed"
                        Log.e(tag, "Login failed: $error")
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Login failed: ${response.code()} ${response.message()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Login error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * User signup
     */
    suspend fun signup(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SignupRequest(username, email, password, fullName)
                val response = apiService.signup(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Save tokens and user data
                        sharedPref.saveAuthTokens(apiResponse.data.accessToken)
                        sharedPref.saveUserData(apiResponse.data.user)
                        
                        Log.d(tag, "Signup successful for user: ${apiResponse.data.user.username}")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Signup failed"
                        Log.e(tag, "Signup failed: $error")
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Signup failed: ${response.code()} ${response.message()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Signup error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Refresh authentication token
     */
    suspend fun refreshToken(): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val currentToken = sharedPref.getAccessToken()
                if (currentToken.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("No token to refresh"))
                }
                
                val response = apiService.refreshToken(NetworkUtils.createAuthHeader(currentToken))
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Save new tokens
                        sharedPref.saveAuthTokens(apiResponse.data.accessToken)
                        sharedPref.saveUserData(apiResponse.data.user)
                        
                        Log.d(tag, "Token refresh successful")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Token refresh failed"
                        Log.e(tag, "Token refresh failed: $error")
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Token refresh failed: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Token refresh error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get current user data
     */
    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sharedPref.getAccessToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("No authentication token"))
                }
                
                val response = apiService.getCurrentUser(NetworkUtils.createAuthHeader(token))
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        sharedPref.saveUserData(apiResponse.data)
                        Log.d(tag, "Current user data retrieved")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to get user data"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to get user data: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Get current user error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(user: User): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sharedPref.getAccessToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("No authentication token"))
                }
                
                val response = apiService.updateProfile(NetworkUtils.createAuthHeader(token), user)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        sharedPref.saveUserData(apiResponse.data)
                        Log.d(tag, "Profile updated successfully")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to update profile"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to update profile: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Update profile error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        sharedPref.logout()
        Log.d(tag, "User logged out")
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPref.isLoggedIn()
    }
    
    /**
     * Get cached user data
     */
    fun getCachedUser(): User? {
        return sharedPref.getUserData()
    }
    
    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return sharedPref.getAccessToken()
    }
}

/**
 * DeepGuard v3.0 - Scan Repository
 * Handles all scanning-related API calls
 */
class ScanRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val sharedPref = SharedPref.getInstance(context)
    private val tag = "ScanRepository"
    
    /**
     * Scan text for harassment
     */
    suspend fun scanText(text: String): Result<ScanResult> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val request = ScanRequest(text = text, scanType = "text")
                val response = apiService.scanText(NetworkUtils.createAuthHeader(token), request)
                
                handleScanResponse(response, "Text scan")
            } catch (e: Exception) {
                Log.e(tag, "Text scan error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Scan media file
     */
    suspend fun scanMedia(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<ScanResult> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val filePart = NetworkUtils.createMultipartFile(fileBytes, fileName, mimeType)
                val scanTypePart = NetworkUtils.createRequestBody("media")
                
                val response = apiService.scanMedia(
                    NetworkUtils.createAuthHeader(token),
                    filePart,
                    scanTypePart
                )
                
                handleScanResponse(response, "Media scan")
            } catch (e: Exception) {
                Log.e(tag, "Media scan error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Scan for deepfakes
     */
    suspend fun scanDeepfake(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<ScanResult> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val filePart = NetworkUtils.createMultipartFile(fileBytes, fileName, mimeType)
                
                val response = apiService.scanDeepfake(
                    NetworkUtils.createAuthHeader(token),
                    filePart
                )
                
                handleScanResponse(response, "Deepfake scan")
            } catch (e: Exception) {
                Log.e(tag, "Deepfake scan error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Start deep scan
     */
    suspend fun startDeepScan(): Result<ScanResult> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val request = ScanRequest(scanType = "deepscan")
                val response = apiService.startDeepScan(NetworkUtils.createAuthHeader(token), request)
                
                handleScanResponse(response, "Deep scan")
            } catch (e: Exception) {
                Log.e(tag, "Deep scan error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get user's scan history
     */
    suspend fun getUserScans(limit: Int = 20, offset: Int = 0): Result<List<ScanResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.getUserScans(
                    NetworkUtils.createAuthHeader(token),
                    limit,
                    offset
                )
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(tag, "Retrieved ${apiResponse.data.size} scan results")
                        Result.success(apiResponse.data)
                    } else {
                        val error = apiResponse?.error ?: "Failed to get scan history"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to get scan history: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Get user scans error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Delete a scan result
     */
    suspend fun deleteScan(scanId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.deleteScan(NetworkUtils.createAuthHeader(token), scanId)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d(tag, "Scan deleted successfully")
                        Result.success("Scan deleted successfully")
                    } else {
                        val error = apiResponse?.error ?: "Failed to delete scan"
                        Log.e(tag, error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Failed to delete scan: ${response.code()}"
                    Log.e(tag, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(tag, "Delete scan error", e)
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
    
    private fun handleScanResponse(
        response: Response<ApiResponse<ScanResult>>,
        scanType: String
    ): Result<ScanResult> {
        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true && apiResponse.data != null) {
                Log.d(tag, "$scanType completed successfully")
                Result.success(apiResponse.data)
            } else {
                val error = apiResponse?.error ?: "$scanType failed"
                Log.e(tag, error)
                Result.failure(Exception(error))
            }
        } else {
            val error = "$scanType failed: ${response.code()} ${response.message()}"
            Log.e(tag, error)
            Result.failure(Exception(error))
        }
    }
}
