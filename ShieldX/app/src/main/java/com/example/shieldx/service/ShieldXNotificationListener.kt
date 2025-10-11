package com.example.shieldx.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.shieldx.MainActivity
import com.example.shieldx.R
import com.example.shieldx.api.ShieldXAPI
import com.example.shieldx.data.AnalysisResponse
import com.example.shieldx.data.NotificationPayload
import kotlinx.coroutines.*
import retrofit2.Response

class ShieldXNotificationListener : NotificationListenerService() {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val api = ShieldXAPI()
    
    companion object {
        private const val TAG = "ShieldXListener"
        private const val ALERT_CHANNEL_ID = "harassment_alerts"
        private const val ALERT_NOTIFICATION_ID = 1001
        
        // Apps to monitor for harassment
        private val MONITORED_APPS = setOf(
            "com.whatsapp",
            "com.facebook.orca",
            "com.instagram.android",
            "org.telegram.messenger",
            "com.android.mms",
            "com.snapchat.android",
            "com.discord",
            "com.twitter.android",
            "com.tiktok.android",
            "com.google.android.apps.messaging"
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ShieldX Notification Listener Service Started")
        createNotificationChannel()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Log.d(TAG, "ShieldX Notification Listener Service Stopped")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            
            // Only monitor specified apps
            if (!MONITORED_APPS.contains(packageName)) {
                return
            }
            
            val notification = sbn.notification
            val extras = notification.extras
            
            // Extract notification content
            val title = extras.getCharSequence("android.title")?.toString()
            val text = extras.getCharSequence("android.text")?.toString()
            val bigText = extras.getCharSequence("android.bigText")?.toString()
            
            val content = bigText ?: text
            
            if (content.isNullOrBlank()) {
                Log.d(TAG, "Skipping notification with no text content from $packageName")
                return
            }
            
            Log.d(TAG, "Processing notification from $packageName: ${content.take(50)}...")
            
            // Create payload for analysis
            val payload = NotificationPayload(
                content = content,
                source = packageName,
                sender = title ?: "Unknown",
                timestamp = System.currentTimeMillis()
            )
            
            // Analyze notification in background
            scope.launch {
                analyzeNotification(payload)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    private suspend fun analyzeNotification(payload: NotificationPayload) {
        try {
            Log.d(TAG, "Sending for analysis: ${payload.content.take(100)}...")
            
            val response: Response<AnalysisResponse> = api.analyzeNotification(payload)
            
            if (response.isSuccessful) {
                response.body()?.let { analysisResponse ->
                    Log.d(TAG, "Analysis result - Harassment: ${analysisResponse.harassment}")
                    
                    if (analysisResponse.harassment.isHarassment) {
                        withContext(Dispatchers.Main) {
                            showHarassmentAlert(payload, analysisResponse)
                        }
                    }
                } ?: Log.w(TAG, "Received null response body")
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                
                // Fallback: local keyword detection
                if (containsHarassmentKeywords(payload.content)) {
                    withContext(Dispatchers.Main) {
                        showFallbackAlert(payload)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during notification analysis", e)
            
            // Fallback: local keyword detection
            if (containsHarassmentKeywords(payload.content)) {
                withContext(Dispatchers.Main) {
                    showFallbackAlert(payload)
                }
            }
        }
    }
    
    private fun showHarassmentAlert(payload: NotificationPayload, analysis: AnalysisResponse) {
        val appName = getAppName(payload.source)
        val confidence = (analysis.harassment.confidence * 100).toInt()
        
        val title = "� THREAT DETECTED - ${confidence}% RISK"
        val message = "⚠️ HARASSMENT ALERT ⚠️\n\n" +
                "🎯 RISK SCORE: ${confidence}%\n" +
                "📱 Source: $appName\n" +
                "👤 From: ${payload.sender}\n" +
                "🔍 Type: ${analysis.harassment.type?.uppercase()}\n" +
                "📊 Severity: ${analysis.harassment.severity?.uppercase()}\n\n" +
                "💬 Message: \"${payload.content.take(50)}${if(payload.content.length > 50) "..." else ""}\"\n\n" +
                "🛡️ ShieldX AI Protection Active"
        
        showAlert(title, message, payload)
        
        Log.w(TAG, "🚨 HARASSMENT ALERT: $appName - RISK SCORE: ${confidence}% - Type: ${analysis.harassment.type}")
    }
    
    private fun showFallbackAlert(payload: NotificationPayload) {
        val appName = getAppName(payload.source)
        
        val title = "⚠️ Potential Harassment"
        val message = "ShieldX detected potentially harmful content in $appName using local detection.\n\n" +
                "From: ${payload.sender}"
        
        showAlert(title, message, payload)
        
        Log.w(TAG, "FALLBACK ALERT: $appName - Local keyword detection")
    }
    
    private fun showAlert(title: String, message: String, payload: NotificationPayload) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Harassment Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for detected harassment in notifications"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun getAppName(packageName: String): String = when (packageName) {
        "com.whatsapp" -> "WhatsApp"
        "com.facebook.orca" -> "Messenger"
        "com.instagram.android" -> "Instagram"
        "org.telegram.messenger" -> "Telegram"
        "com.android.mms" -> "Messages"
        "com.snapchat.android" -> "Snapchat"
        "com.discord" -> "Discord"
        "com.twitter.android" -> "Twitter"
        "com.tiktok.android" -> "TikTok"
        "com.google.android.apps.messaging" -> "Messages"
        else -> packageName.substringAfterLast(".")
    }
    
    // Fallback keyword detection
    private fun containsHarassmentKeywords(content: String): Boolean {
        val harassmentKeywords = setOf(
            "kill yourself", "kys", "die", "hate you", "worthless", "stupid",
            "ugly", "fat", "loser", "pathetic", "disgusting", "awful",
            "terrible", "horrible", "useless", "idiot", "moron", "freak"
        )
        
        val lowercaseContent = content.lowercase()
        return harassmentKeywords.any { lowercaseContent.contains(it) }
    }
}