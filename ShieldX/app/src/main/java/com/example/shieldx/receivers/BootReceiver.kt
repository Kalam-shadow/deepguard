package com.example.shieldx.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.shieldx.services.NotificationListenerService
import com.example.shieldx.utils.SharedPref

/**
 * Boot receiver to automatically start DeepGuard monitoring
 * after device boot or app update
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DeepGuardBootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "Boot receiver triggered with action: $action")
        
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                handleBootOrUpdate(context)
            }
        }
    }
    
    private fun handleBootOrUpdate(context: Context) {
        val sharedPref = SharedPref.getInstance(context)
        
        // Check if user had monitoring enabled before boot/update
        val wasMonitoringActive = sharedPref.isMonitoringActive()
        val autoStartEnabled = sharedPref.getBooleanValue("auto_start_monitoring", false)
        
        Log.i(TAG, "Boot/Update - Was monitoring active: $wasMonitoringActive, Auto-start enabled: $autoStartEnabled")
        
        // Only auto-start if user had it enabled or specifically requested auto-start
        if (wasMonitoringActive || autoStartEnabled) {
            // Check if notification listener permission is still granted
            if (NotificationListenerService.isNotificationServiceEnabled(context)) {
                try {
                    // Start the notification listener service
                    val serviceIntent = Intent(context, NotificationListenerService::class.java)
                    context.startForegroundService(serviceIntent)
                    
                    Log.i(TAG, "NotificationListenerService started after boot/update")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start NotificationListenerService after boot/update", e)
                }
            } else {
                Log.w(TAG, "Notification listener permission not granted, cannot start monitoring")
                // Reset monitoring state since permission is not available
                sharedPref.setMonitoringActive(false)
            }
        } else {
            Log.i(TAG, "Monitoring was not active before boot/update, not auto-starting")
        }
    }
}
