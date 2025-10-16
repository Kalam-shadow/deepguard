package com.example.shieldx.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/**
 * DeepGuard v3.0 - Permission Utils
 * Handles all app permissions including dangerous permissions
 */
object PermissionUtils {
    
    // Permission codes
    const val PERMISSION_REQUEST_CODE = 1001
    const val CAMERA_PERMISSION_CODE = 1002
    const val STORAGE_PERMISSION_CODE = 1003
    const val NOTIFICATION_PERMISSION_CODE = 1004
    
    // Required permissions
    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    val NOTIFICATION_PERMISSIONS = arrayOf(
        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
        "android.permission.POST_NOTIFICATIONS" // For Android 13+
    )
    
    val ALL_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
    )
    
    /**
     * Check if permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if all permissions are granted
     */
    fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(context, it) }
    }
    
    /**
     * Request permission using ActivityCompat
     */
    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }
    
    /**
     * Request multiple permissions using ActivityCompat
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
    
    /**
     * Check and request storage permissions
     */
    fun checkStoragePermissions(activity: Activity, callback: PermissionCallback) {
        if (arePermissionsGranted(activity, STORAGE_PERMISSIONS)) {
            callback.onPermissionGranted()
        } else {
            requestPermissionsWithDexter(activity, STORAGE_PERMISSIONS.toList(), callback)
        }
    }
    
    /**
     * Check and request camera permissions
     */
    fun checkCameraPermissions(activity: Activity, callback: PermissionCallback) {
        if (arePermissionsGranted(activity, CAMERA_PERMISSIONS)) {
            callback.onPermissionGranted()
        } else {
            requestPermissionsWithDexter(activity, CAMERA_PERMISSIONS.toList(), callback)
        }
    }
    
    /**
     * Check notification listener permission
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(packageName) == true
    }
    
    /**
     * Request notification listener permission
     */
    fun requestNotificationListenerPermission(context: Context) {
        val intent = android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        context.startActivity(intent)
    }
    
    /**
     * Request permissions using Dexter library
     */
    private fun requestPermissionsWithDexter(
        activity: Activity,
        permissions: List<String>,
        callback: PermissionCallback
    ) {
        Dexter.withActivity(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (it.areAllPermissionsGranted()) {
                            callback.onPermissionGranted()
                        } else {
                            callback.onPermissionDenied(it.deniedPermissionResponses.map { denied -> denied.permissionName })
                        }
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }
    
    /**
     * Handle permission result
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        callback: PermissionCallback
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_CODE,
            CAMERA_PERMISSION_CODE,
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    callback.onPermissionGranted()
                } else {
                    callback.onPermissionDenied(permissions.toList())
                }
            }
        }
    }
    
    /**
     * Show permission rationale dialog
     */
    fun showPermissionRationale(
        activity: Activity,
        title: String,
        message: String,
        permissions: Array<String>,
        requestCode: Int
    ) {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ ->
                requestPermissions(activity, permissions, requestCode)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Permission callback interface
     */
    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied(deniedPermissions: List<String>)
    }
}

/**
 * File Utils for handling file operations
 */
object FileUtils {
    
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }
    
    fun isImageFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
    
    fun isVideoFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return extension in listOf("mp4", "avi", "mov", "mkv", "wmv", "flv", "webm")
    }
    
    fun isDocumentFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return extension in listOf("pdf", "doc", "docx", "txt", "rtf")
    }
    
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", size, units[unitIndex])
    }
}


