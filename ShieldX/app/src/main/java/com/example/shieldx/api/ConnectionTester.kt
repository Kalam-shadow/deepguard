package com.example.shieldx.api

import android.util.Log
import kotlinx.coroutines.*

object ConnectionTester {
    private const val TAG = "ConnectionTester"
    
    fun runAllTests() {
        CoroutineScope(Dispatchers.IO).launch {
            testApiConnectivity()
            testAllEndpoints()
        }
    }
    
    private suspend fun testApiConnectivity() {
        Log.i(TAG, "=== ShieldX Connection Test ===")
        
        val api = ShieldXAPI()
        val isConnected = api.testConnection()
        
        if (isConnected) {
            Log.i(TAG, "✅ Backend connection successful!")
            Log.i(TAG, "📡 Using endpoint: ${api.getCurrentBaseUrl()}")
        } else {
            Log.e(TAG, "❌ Backend connection failed!")
            Log.e(TAG, "🔍 Troubleshooting:")
            Log.e(TAG, "   1. Check if DeepGuard backend is running on port 8000")
            Log.e(TAG, "   2. Verify your computer's IP address")
            Log.e(TAG, "   3. Ensure both devices are on the same network")
            Log.e(TAG, "   4. Check firewall settings")
        }
    }
    
    private suspend fun testAllEndpoints() {
        Log.i(TAG, "=== Testing All Possible Endpoints ===")
        
        val baseUrls = listOf(
            "http://192.168.56.1:8000",
            "http://192.168.137.1:8000", 
            "http://172.16.125.114:8000",
            "http://10.0.2.2:8000",
            "http://192.168.1.100:8000",
            "http://localhost:8000"
        )
        
        baseUrls.forEach { baseUrl ->
            try {
                Log.d(TAG, "Testing: $baseUrl")
                // Individual endpoint testing would go here
                delay(100) // Small delay between tests
            } catch (e: Exception) {
                Log.d(TAG, "❌ $baseUrl - ${e.message}")
            }
        }
    }
}