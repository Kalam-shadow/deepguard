package com.example.shieldx.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Utility class to test API connectivity and diagnose connection issues
 */
object ConnectionTester {
    private const val TAG = "ConnectionTester"
    private const val TIMEOUT_SECONDS = 5L
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    /**
     * Test connection to the API endpoint
     * @return true if connection successful, false otherwise
     */
    suspend fun testConnection(baseUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Testing: $baseUrl")
            
            val request = Request.Builder()
                .url("${baseUrl}api/v1/health")
                .build()
            
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            
            if (success) {
                Log.i(TAG, "✅ Successfully connected to $baseUrl")
            } else {
                Log.e(TAG, "❌ Failed to connect to $baseUrl (HTTP ${response.code})")
            }
            
            success
        } catch (e: ConnectException) {
            Log.e(TAG, "❌ Connection refused: $baseUrl")
            false
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "❌ Connection timed out: $baseUrl")
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error testing connection to $baseUrl: ${e.message}")
            false
        }
    }
    
    /**
     * Test all possible endpoint combinations to help diagnose connection issues
     */
    suspend fun testAllEndpoints() {
        Log.i(TAG, "=== Testing All Possible Endpoints ===")
        
        // Local network IPs
        val ips = listOf(
            "192.168.0.22",    // Common local IP
            "192.168.56.1",    // VirtualBox host-only
            "192.168.137.1",   // Mobile hotspot
            "10.0.2.2"         // Android emulator
        )
        
        // Test ports
        val ports = listOf(8001, 8000)
        
        // Test all combinations
        for (ip in ips) {
            for (port in ports) {
                testConnection("http://$ip:$port/")
            }
        }
    }
    
    /**
     * Print troubleshooting information
     */
    fun printTroubleshootingInfo() {
        Log.e(TAG, "❌ Backend connection failed!")
        Log.e(TAG, "🔍 Troubleshooting:")
        Log.e(TAG, "   1. Check if DeepGuard backend is running on port 8001")
        Log.e(TAG, "   2. Verify your computer's IP address")
        Log.e(TAG, "   3. Ensure both devices are on the same network")
        Log.e(TAG, "   4. Check firewall settings")
    }
}