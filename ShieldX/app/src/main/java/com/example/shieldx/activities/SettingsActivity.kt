package com.example.shieldx.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.shieldx.R
import com.example.shieldx.activities.LoginActivity
import com.example.shieldx.databinding.ActivitySettingsBinding
import com.example.shieldx.utils.SharedPref

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPref: SharedPref
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPref = SharedPref.getInstance(this)
        
        setupUI()
        loadSettings()
        setupBottomNavigation()
    }
    
    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        
        // Account Settings
        binding.layoutProfile.setOnClickListener {
            // Navigate to profile edit
            showComingSoon("Profile editing")
        }
        
        binding.layoutChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        // Protection Settings
        binding.switchRealTimeProtection.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setRealTimeModeEnabled(isChecked)
            if (isChecked) {
                Toast.makeText(this, "Real-time protection enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Real-time protection disabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.switchAutoBlock.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setAutoBlockEnabled(isChecked)
            if (isChecked) {
                showAutoBlockWarning()
            }
        }
        
        // Notification Settings
        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setPushNotificationsEnabled(isChecked)
        }
        
        binding.switchSoundAlerts.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setSoundAlertsEnabled(isChecked)
        }
        
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setVibrationEnabled(isChecked)
        }
        
        // Privacy Settings
        binding.switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setAnalyticsEnabled(isChecked)
            if (isChecked) {
                showAnalyticsInfo()
            }
        }
        
        // Monitoring Settings
        binding.layoutNotificationMonitoring.setOnClickListener {
            openNotificationMonitoringSettings()
        }
        
        // About Section
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
        
        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun loadSettings() {
        // Load protection settings
        binding.switchRealTimeProtection.isChecked = sharedPref.isRealTimeModeEnabled()
        binding.switchAutoBlock.isChecked = sharedPref.isAutoBlockEnabled()
        
        // Load notification settings
        binding.switchPushNotifications.isChecked = sharedPref.isPushNotificationsEnabled()
        binding.switchSoundAlerts.isChecked = sharedPref.isSoundAlertsEnabled()
        binding.switchVibration.isChecked = sharedPref.isVibrationEnabled()
        
        // Load privacy settings
        binding.switchAnalytics.isChecked = sharedPref.isAnalyticsEnabled()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_deepscan -> {
                    startActivity(Intent(this, DeepScanActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_deepfake -> {
                    startActivity(Intent(this, DeepfakeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }
    
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, which ->
                // Implement password change logic
                Toast.makeText(this, "Password change functionality coming soon", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showAutoBlockWarning() {
        AlertDialog.Builder(this)
            .setTitle("Auto Block Enabled")
            .setMessage("Auto block will automatically block notifications detected as threats. This may occasionally block legitimate messages. You can review blocked content in the analytics section.")
            .setPositiveButton("I Understand", null)
            .show()
    }
    
    private fun showAnalyticsInfo() {
        AlertDialog.Builder(this)
            .setTitle("Anonymous Analytics")
            .setMessage("Anonymous analytics help us improve DeepGuard by collecting usage statistics. No personal information or message content is shared. You can disable this at any time.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About DeepGuard")
            .setMessage("""
                DeepGuard v3.0
                AI-Based Cyber Harassment and Deepfake Detection
                
                DeepGuard uses advanced artificial intelligence to protect you from cyber harassment and deepfake content. Our real-time monitoring system analyzes incoming notifications and media files to detect potential threats.
                
                Features:
                • Real-time notification monitoring
                • Deepfake detection for images and videos
                • Harassment detection in text messages
                • Advanced analytics and reporting
                • Privacy-focused design
                
                Developed with ❤️ for digital safety
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("Visit Website") { _, _ ->
                // Open website
                showComingSoon("Website")
            }
            .show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? You will need to login again to access your account.")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        // Clear user data
        sharedPref.logout()
        
        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    
    private fun openNotificationMonitoringSettings() {
        val intent = Intent(this, NotificationMonitoringActivity::class.java)
        startActivity(intent)
    }
    
    private fun showComingSoon(feature: String) {
        Toast.makeText(this, "$feature coming soon!", Toast.LENGTH_SHORT).show()
    }
}
