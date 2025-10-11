package com.example.shieldx.api

import com.example.shieldx.data.AnalysisResponse
import com.example.shieldx.data.NotificationPayload
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class ShieldXAPI {
    companion object {
        private const val TAG = "ShieldXAPI"
        
        // Try these IP addresses in order - updated with current network IPs
        private val BASE_URLS = listOf(// Current network IP on port 8001 (PRIMARY)
            "http://192.168.56.1:8001",    // VirtualBox host IP
            "http://192.168.137.1:8001",   // Mobile hotspot IP
            "http://10.0.2.2:8001",        // Android emulator host
            "http://192.168.0.22:8000",    // Fallback to port 8000
            "http://192.168.56.1:8000",    // Fallback VirtualBox
        )
        
        private const val AUTH_TOKEN = "Bearer your-jwt-token-here" // Replace with actual token
    }
    
    private var currentApiService: ShieldXApiService? = null
    private var workingBaseUrl: String? = null
    
    init {
        initializeApiService()
    }
    
    private fun initializeApiService() {
        // Create HTTP client with logging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        
        // Try each base URL until one works
        for (baseUrl in BASE_URLS) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                currentApiService = retrofit.create(ShieldXApiService::class.java)
                workingBaseUrl = baseUrl
                Log.i(TAG, "API service initialized with base URL: $baseUrl")
                break
                
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize API with $baseUrl: ${e.message}")
            }
        }
        
        if (currentApiService == null) {
            Log.e(TAG, "Failed to initialize API service with any base URL")
        }
    }
    
    suspend fun analyzeNotification(payload: NotificationPayload): Response<AnalysisResponse> {
        val apiService = currentApiService
            ?: throw IllegalStateException("API service not initialized")
        
        return try {
            Log.d(TAG, "Analyzing notification: ${payload.content.take(50)}...")
            Log.d(TAG, "Using endpoint: $workingBaseUrl/api/v1/mobile/analyze-notification")
            
            // Try with authentication first
            try {
                val response = apiService.analyzeNotification(AUTH_TOKEN, payload)
                Log.d(TAG, "API response with auth: ${response.code()}")
                response
            } catch (authException: Exception) {
                Log.w(TAG, "Auth failed, trying without auth: ${authException.message}")
                // Fallback to no auth
                val response = apiService.analyzeNotificationWithoutAuth(payload)
                Log.d(TAG, "API response without auth: ${response.code()}")
                response
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            throw e
        }
    }
    
    fun getCurrentBaseUrl(): String? = workingBaseUrl
    
    // Helper method for testing connectivity
    suspend fun testConnection(): Boolean {
        return try {
            val testPayload = NotificationPayload(
                content = "Test message",
                source = "com.test",
                sender = "Test",
                timestamp = System.currentTimeMillis()
            )
            
            val response = analyzeNotification(testPayload)
            val success = response.isSuccessful || response.code() in 400..499 // 4xx means server is reachable
            
            Log.i(TAG, "Connection test result: $success (HTTP ${response.code()})")
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            false
        }
    }
}