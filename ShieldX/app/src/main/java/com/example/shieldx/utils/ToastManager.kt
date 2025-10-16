package com.example.shieldx.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.util.concurrent.atomic.AtomicLong

/**
 * DeepGuard v3.0 - Toast Manager
 * Prevents toast spam by debouncing and queueing toast messages
 * 
 * Features:
 * - Prevents multiple toasts from showing simultaneously
 * - Debounces identical messages
 * - Queues messages with cooldown period
 * - Automatic cancellation of previous toasts
 */
object ToastManager {
    private const val TAG = "ToastManager"
    private const val MIN_TOAST_INTERVAL = 1000L // 1 second minimum between toasts
    private const val DUPLICATE_MESSAGE_INTERVAL = 3000L // 3 seconds for duplicate messages
    
    private var currentToast: Toast? = null
    private val lastToastTime = AtomicLong(0)
    private val lastMessages = mutableMapOf<String, Long>()
    private val handler = Handler(Looper.getMainLooper())
    private val toastQueue = mutableListOf<ToastMessage>()
    private var isProcessing = false
    
    data class ToastMessage(
        val context: Context,
        val message: String,
        val duration: Int,
        val priority: Int = 0
    )
    
    /**
     * Show a short toast message
     */
    @JvmStatic
    fun showShort(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show a long toast message
     */
    @JvmStatic
    fun showLong(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }
    
    /**
     * Show a toast message with custom duration
     */
    @JvmStatic
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT, priority: Int = 0) {
        if (message.isBlank()) return
        
        // Check if this is a duplicate message within the cooldown period
        val now = System.currentTimeMillis()
        val lastShown = lastMessages[message] ?: 0
        
        if (now - lastShown < DUPLICATE_MESSAGE_INTERVAL) {
            // Skip duplicate message
            return
        }
        
        // Add to queue
        synchronized(toastQueue) {
            // Remove any existing duplicate messages in queue
            toastQueue.removeAll { it.message == message }
            
            // Add new message
            val toastMessage = ToastMessage(context, message, duration, priority)
            if (priority > 0) {
                // Insert high priority messages at the front
                toastQueue.add(0, toastMessage)
            } else {
                toastQueue.add(toastMessage)
            }
        }
        
        processQueue()
    }
    
    /**
     * Show an important toast that bypasses some restrictions
     */
    @JvmStatic
    fun showImportant(context: Context, message: String, duration: Int = Toast.LENGTH_LONG) {
        show(context, message, duration, priority = 10)
    }
    
    /**
     * Process the toast queue
     */
    private fun processQueue() {
        if (isProcessing) return
        
        synchronized(toastQueue) {
            if (toastQueue.isEmpty()) return
            
            isProcessing = true
            val now = System.currentTimeMillis()
            val timeSinceLastToast = now - lastToastTime.get()
            
            if (timeSinceLastToast < MIN_TOAST_INTERVAL) {
                // Wait before showing next toast
                handler.postDelayed({
                    isProcessing = false
                    processQueue()
                }, MIN_TOAST_INTERVAL - timeSinceLastToast)
                return
            }
            
            // Get next toast to show
            val toastMessage = toastQueue.removeAt(0)
            
            // Cancel previous toast
            currentToast?.cancel()
            
            // Show new toast on main thread
            handler.post {
                try {
                    currentToast = Toast.makeText(
                        toastMessage.context.applicationContext,
                        toastMessage.message,
                        toastMessage.duration
                    )
                    currentToast?.show()
                    
                    lastToastTime.set(System.currentTimeMillis())
                    lastMessages[toastMessage.message] = System.currentTimeMillis()
                    
                    // Schedule next toast
                    handler.postDelayed({
                        isProcessing = false
                        processQueue()
                    }, if (toastMessage.duration == Toast.LENGTH_LONG) 3500L else 2000L)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error showing toast", e)
                    isProcessing = false
                }
            }
        }
    }
    
    /**
     * Clear all pending toasts
     */
    @JvmStatic
    fun clear() {
        synchronized(toastQueue) {
            toastQueue.clear()
        }
        currentToast?.cancel()
        currentToast = null
        isProcessing = false
        lastMessages.clear()
    }
    
    /**
     * Cancel current toast
     */
    @JvmStatic
    fun cancel() {
        currentToast?.cancel()
        currentToast = null
    }
}
