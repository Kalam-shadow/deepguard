package com.example.shieldx.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shieldx.R
import com.example.shieldx.databinding.ActivityNotificationMonitoringBinding
import com.example.shieldx.services.NotificationListenerService
import com.example.shieldx.utils.SharedPref
import kotlinx.coroutines.launch

class NotificationMonitoringActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNotificationMonitoringBinding
    private lateinit var sharedPref: SharedPref
    private val updateHandler = Handler(Looper.getMainLooper())
    private var isActivityActive = false
    
    // BroadcastReceiver to listen for stats updates
    private val statsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.shieldx.STATS_UPDATED") {
                // Update UI with new stats
                updateMonitoringStatus()
                updateLiveStats()
            }
        }
    }
    
    // Runnable for periodic updates
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isActivityActive && sharedPref.isMonitoringActive()) {
                updateMonitoringStatus()
                updateLiveStats()
                updateHandler.postDelayed(this, 2000) // Update every 2 seconds
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPref = SharedPref.getInstance(this)
        
        // Register broadcast receiver for stats updates
        val filter = IntentFilter("com.example.shieldx.STATS_UPDATED")
        registerReceiver(statsUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        
        setupUI()
        loadSettings()
        checkNotificationPermission()
    }
    
    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        
        // Main monitoring toggle
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (NotificationListenerService.isNotificationServiceEnabled(this)) {
                    startMonitoring()
                } else {
                    binding.switchMonitoring.isChecked = false
                    showPermissionDialog()
                }
            } else {
                stopMonitoring()
            }
        }
        
        // Harassment detection toggle
        binding.switchHarassmentDetection.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setHarassmentDetectionEnabled(isChecked)
            updateSettingsDescription()
        }
        
        // Deepfake detection toggle
        binding.switchDeepfakeDetection.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setDeepfakeDetectionEnabled(isChecked)
            updateSettingsDescription()
        }
        
        // Auto block toggle
        binding.switchAutoBlock.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setAutoBlockEnabled(isChecked)
            if (isChecked) {
                showAutoBlockWarning()
            }
            updateSettingsDescription()
        }
        
        // Real-time analysis toggle
        binding.switchRealTimeAnalysis.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setRealTimeModeEnabled(isChecked)
            updateSettingsDescription()
        }
        
        // Advanced analysis toggle
        binding.switchAdvancedAnalysis.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setAdvancedAnalysis(isChecked)
            updateSettingsDescription()
        }
        
        // Auto start toggle
        binding.switchAutoStart.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setAutoStartMonitoring(isChecked)
        }
        
        // Threshold slider
        binding.seekBarThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvThresholdValue.text = "$progress%"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val threshold = seekBar?.progress ?: 50
                sharedPref.setNotificationThreshold(threshold)
                updateSettingsDescription()
            }
        })
        
        // Configure monitored apps
        binding.layoutMonitoredApps.setOnClickListener {
            showMonitoredAppsDialog()
        }
        
        // Configure trusted contacts
        binding.layoutTrustedContacts.setOnClickListener {
            showTrustedContactsDialog()
        }
        
        // Configure quiet hours
        binding.layoutQuietHours.setOnClickListener {
            showQuietHoursDialog()
        }
        
        // View monitoring statistics
        binding.layoutStatistics.setOnClickListener {
            showMonitoringStatistics()
        }
        
        // Export settings
        binding.btnExportSettings.setOnClickListener {
            exportSettings()
        }
        
        // Import settings
        binding.btnImportSettings.setOnClickListener {
            importSettings()
        }
    }
    
    private fun loadSettings() {
        // Load main switches
        binding.switchMonitoring.isChecked = sharedPref.isMonitoringActive()
        binding.switchHarassmentDetection.isChecked = sharedPref.isHarassmentDetectionEnabled()
        binding.switchDeepfakeDetection.isChecked = sharedPref.isDeepfakeDetectionEnabled()
        binding.switchAutoBlock.isChecked = sharedPref.isAutoBlockEnabled()
        binding.switchRealTimeAnalysis.isChecked = sharedPref.isRealTimeModeEnabled()
        binding.switchAdvancedAnalysis.isChecked = sharedPref.getAdvancedAnalysis()
        binding.switchAutoStart.isChecked = sharedPref.isAutoStartMonitoringEnabled()
        
        // Load threshold
        val threshold = sharedPref.getNotificationThreshold()
        binding.seekBarThreshold.progress = threshold
        binding.tvThresholdValue.text = "$threshold%"
        
        updateSettingsDescription()
        updateMonitoringStatus()
    }
    
    private fun checkNotificationPermission() {
        if (!NotificationListenerService.isNotificationServiceEnabled(this)) {
            binding.layoutPermissionWarning.visibility = android.view.View.VISIBLE
            
            binding.btnGrantPermission.setOnClickListener {
                NotificationListenerService.openNotificationAccessSettings(this)
            }
        } else {
            binding.layoutPermissionWarning.visibility = android.view.View.GONE
        }
    }
    
    private fun startMonitoring() {
        lifecycleScope.launch {
            try {
                val serviceIntent = Intent(this@NotificationMonitoringActivity, NotificationListenerService::class.java)
                startForegroundService(serviceIntent)
                
                sharedPref.setMonitoringActive(true)
                updateMonitoringStatus()
                
                Toast.makeText(this@NotificationMonitoringActivity, 
                    "🛡️ Protection enabled", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.switchMonitoring.isChecked = false
                Toast.makeText(this@NotificationMonitoringActivity, 
                    "Failed to start monitoring", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun stopMonitoring() {
        val serviceIntent = Intent(this, NotificationListenerService::class.java)
        stopService(serviceIntent)
        
        sharedPref.setMonitoringActive(false)
        updateMonitoringStatus()
        
        Toast.makeText(this, "Protection disabled", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateMonitoringStatus() {
        if (sharedPref.isMonitoringActive()) {
            binding.tvMonitoringStatus.text = "🟢 Active"
            binding.tvMonitoringStatus.setTextColor(getColor(R.color.success_color))
            
            val startTime = sharedPref.getMonitoringStartTime()
            if (startTime > 0) {
                val duration = System.currentTimeMillis() - startTime
                val hours = duration / (1000 * 60 * 60)
                val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
                binding.tvMonitoringDuration.text = "Active for ${hours}h ${minutes}m"
            }
        } else {
            binding.tvMonitoringStatus.text = "🔴 Inactive"
            binding.tvMonitoringStatus.setTextColor(getColor(R.color.danger_color))
            binding.tvMonitoringDuration.text = "Not running"
        }
    }
    
    private fun updateSettingsDescription() {
        val enabledFeatures = mutableListOf<String>()
        
        if (sharedPref.isHarassmentDetectionEnabled()) enabledFeatures.add("Harassment Detection")
        if (sharedPref.isDeepfakeDetectionEnabled()) enabledFeatures.add("Deepfake Detection")
        if (sharedPref.isAutoBlockEnabled()) enabledFeatures.add("Auto Block")
        if (sharedPref.isRealTimeModeEnabled()) enabledFeatures.add("Real-time Analysis")
        if (sharedPref.getAdvancedAnalysis()) enabledFeatures.add("Advanced Analysis")
        
        val description = if (enabledFeatures.isNotEmpty()) {
            "Active: " + enabledFeatures.joinToString(", ")
        } else {
            "No protection features enabled"
        }
        
        binding.tvActiveFeatures.text = description
        
        // Update threshold description
        val threshold = binding.seekBarThreshold.progress
        val thresholdDesc = when {
            threshold < 30 -> "Very Sensitive (may have false positives)"
            threshold < 50 -> "Sensitive"
            threshold < 70 -> "Balanced (recommended)"
            threshold < 85 -> "Conservative"
            else -> "Very Conservative (may miss some threats)"
        }
        binding.tvThresholdDescription.text = thresholdDesc
    }
    
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("DeepGuard needs notification access to monitor incoming messages for threats. This permission allows the app to read notification content for analysis but does not store or share your personal data.")
            .setPositiveButton("Grant Permission") { _, _ ->
                NotificationListenerService.openNotificationAccessSettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAutoBlockWarning() {
        AlertDialog.Builder(this)
            .setTitle("Auto Block Warning")
            .setMessage("Auto block will automatically hide notifications detected as threats. While this provides immediate protection, it may occasionally block legitimate messages. You can review blocked content in the monitoring statistics.")
            .setPositiveButton("I Understand", null)
            .setNeutralButton("View Statistics") { _, _ ->
                showMonitoringStatistics()
            }
            .show()
    }
    
    private fun showMonitoredAppsDialog() {
        val apps = arrayOf(
            "WhatsApp", "Messenger", "Instagram", "Telegram", "SMS", 
            "Snapchat", "Signal", "Viber", "Skype", "Discord",
            "Twitter", "LinkedIn", "WeChat", "LINE", "KakaoTalk"
        )
        
        val checkedItems = BooleanArray(apps.size) { true } // Default all enabled
        
        AlertDialog.Builder(this)
            .setTitle("Monitored Apps")
            .setMultiChoiceItems(apps, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Save") { _, _ ->
                val selectedApps = apps.filterIndexed { index, _ -> checkedItems[index] }
                sharedPref.setMonitoredApps(selectedApps.joinToString(","))
                Toast.makeText(this, "Monitored apps updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showTrustedContactsDialog() {
        val currentContacts = sharedPref.getTrustedContacts()
        
        val editText = android.widget.EditText(this)
        editText.hint = "Enter contact names separated by commas"
        editText.setText(currentContacts)
        
        AlertDialog.Builder(this)
            .setTitle("Trusted Contacts")
            .setMessage("Messages from trusted contacts will have reduced threat detection sensitivity.")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val contacts = editText.text.toString().trim()
                sharedPref.setTrustedContacts(contacts)
                Toast.makeText(this, "Trusted contacts updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showQuietHoursDialog() {
        // Simple implementation - in a real app you'd use time pickers
        val options = arrayOf(
            "Disabled",
            "22:00 - 07:00 (Night)",
            "12:00 - 14:00 (Lunch)",
            "18:00 - 20:00 (Dinner)",
            "Custom..."
        )
        
        AlertDialog.Builder(this)
            .setTitle("Quiet Hours")
            .setMessage("During quiet hours, only critical threats will trigger notifications.")
            .setSingleChoiceItems(options, 0) { dialog, which ->
                when (which) {
                    0 -> sharedPref.setQuietHoursEnabled(false)
                    1 -> {
                        sharedPref.setQuietHoursEnabled(true)
                        sharedPref.setQuietHoursStart(22)
                        sharedPref.setQuietHoursEnd(7)
                    }
                    2 -> {
                        sharedPref.setQuietHoursEnabled(true)
                        sharedPref.setQuietHoursStart(12)
                        sharedPref.setQuietHoursEnd(14)
                    }
                    3 -> {
                        sharedPref.setQuietHoursEnabled(true)
                        sharedPref.setQuietHoursStart(18)
                        sharedPref.setQuietHoursEnd(20)
                    }
                    4 -> {
                        Toast.makeText(this, "Custom quiet hours coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
                Toast.makeText(this, "Quiet hours updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showMonitoringStatistics() {
        val scanned = sharedPref.getIntValue("notifications_scanned", 0)
        val detected = sharedPref.getIntValue("threats_detected", 0)
        val blocked = sharedPref.getIntValue("threats_blocked", 0)
        val warnings = sharedPref.getIntValue("warnings_sent", 0)
        
        val stats = """
            📊 Monitoring Statistics
            
            Notifications Scanned: $scanned
            Threats Detected: $detected
            Threats Blocked: $blocked
            Warnings Sent: $warnings
            
            Detection Rate: ${if (scanned > 0) String.format("%.2f", (detected.toFloat() / scanned) * 100) else "0.00"}%
            Block Rate: ${if (detected > 0) String.format("%.2f", (blocked.toFloat() / detected) * 100) else "0.00"}%
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Monitoring Statistics")
            .setMessage(stats)
            .setPositiveButton("OK", null)
            .setNeutralButton("Reset") { _, _ ->
                resetStatistics()
            }
            .show()
    }
    
    private fun resetStatistics() {
        AlertDialog.Builder(this)
            .setTitle("Reset Statistics")
            .setMessage("Are you sure you want to reset all monitoring statistics? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                sharedPref.saveIntValue("notifications_scanned", 0)
                sharedPref.saveIntValue("threats_detected", 0)
                sharedPref.saveIntValue("threats_blocked", 0)
                sharedPref.saveIntValue("warnings_sent", 0)
                Toast.makeText(this, "Statistics reset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportSettings() {
        // Simple implementation - in a real app you'd export to file
        val settings = """
            Harassment Detection: ${sharedPref.isHarassmentDetectionEnabled()}
            Deepfake Detection: ${sharedPref.isDeepfakeDetectionEnabled()}
            Auto Block: ${sharedPref.isAutoBlockEnabled()}
            Real-time Analysis: ${sharedPref.isRealTimeModeEnabled()}
            Threshold: ${sharedPref.getNotificationThreshold()}%
            Auto Start: ${sharedPref.isAutoStartMonitoringEnabled()}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Export Settings")
            .setMessage("Settings exported to clipboard:\n\n$settings")
            .setPositiveButton("OK") { _, _ ->
                // Copy to clipboard
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("DeepGuard Settings", settings)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Settings copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun importSettings() {
        Toast.makeText(this, "Import settings functionality coming soon", Toast.LENGTH_SHORT).show()
    }
    
    override fun onResume() {
        super.onResume()
        isActivityActive = true
        checkNotificationPermission()
        updateMonitoringStatus()
        updateLiveStats()
        
        // Start periodic updates if monitoring is active
        if (sharedPref.isMonitoringActive()) {
            updateHandler.postDelayed(updateRunnable, 2000)
        }
    }
    
    override fun onPause() {
        super.onPause()
        isActivityActive = false
        updateHandler.removeCallbacks(updateRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        try {
            unregisterReceiver(statsUpdateReceiver)
        } catch (e: Exception) {
            // Receiver already unregistered
        }
        updateHandler.removeCallbacks(updateRunnable)
    }
    
    /**
     * Update live statistics display
     */
    private fun updateLiveStats() {
        val scanned = sharedPref.getIntValue("notifications_scanned", 0)
        val detected = sharedPref.getIntValue("threats_detected", 0)
        val blocked = sharedPref.getIntValue("threats_blocked", 0)
        val warnings = sharedPref.getIntValue("warnings_sent", 0)
        
        // Update statistics text (if you have TextViews for live display)
        // binding.tvScannedCount?.text = scanned.toString()
        // binding.tvDetectedCount?.text = detected.toString()
        // binding.tvBlockedCount?.text = blocked.toString()
        
        // Update the active features description with live count
        val detectionRate = if (scanned > 0) {
            String.format("%.1f", (detected.toFloat() / scanned) * 100)
        } else {
            "0.0"
        }
        
        // Add stats to the monitoring duration text
        if (sharedPref.isMonitoringActive()) {
            binding.tvMonitoringDuration.text = 
                "Scanned: $scanned | Threats: $detected | Blocked: $blocked"
        }
    }
}
