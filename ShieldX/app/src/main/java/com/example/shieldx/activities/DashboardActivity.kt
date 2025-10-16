package com.example.shieldx.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shieldx.R
import com.example.shieldx.databinding.ActivityDashboardBinding
import com.example.shieldx.viewmodel.AuthViewModel
import com.example.shieldx.viewmodel.AnalyticsViewModel
import com.example.shieldx.utils.GraphUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * DeepGuard v3.0 - Dashboard Activity
 * Main dashboard with bottom navigation and overview cards
 */
class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var analyticsViewModel: AnalyticsViewModel
    private val updateHandler = Handler(Looper.getMainLooper())
    
    // BroadcastReceiver to listen for real-time threat detection updates
    private val statsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.shieldx.STATS_UPDATED") {
                // Refresh dashboard data when threats are detected
                analyticsViewModel.refreshData()
            }
        }
    }
    
    // Auto-refresh runnable for periodic updates
    private val refreshRunnable = object : Runnable {
        override fun run() {
            analyticsViewModel.refreshData()
            updateHandler.postDelayed(this, 10000) // Refresh every 10 seconds
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModels
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        analyticsViewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        
        // Register broadcast receiver for real-time updates
        val filter = IntentFilter("com.example.shieldx.STATS_UPDATED")
        registerReceiver(statsUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        
        // Initialize UI
        setupBottomNavigation()
        setupClickListeners()
        observeViewModels()
        
        // Load initial data
        loadDashboardData()
    }
    

    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, refresh data
                    analyticsViewModel.refreshData()
                    true
                }
                R.id.nav_deepscan -> {
                    startActivity(Intent(this, DeepScanActivity::class.java))
                    true
                }
                R.id.nav_deepfake -> {
                    startActivity(Intent(this, DeepfakeActivity::class.java))
                    true
                }
                R.id.nav_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        // Set home as selected
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
    
    private fun setupClickListeners() {
        // TODO: Update click listeners to match actual layout IDs
        // Currently using placeholder implementation
    }
    
    private fun observeViewModels() {
        // Observe current user
        authViewModel.currentUser.observe(this) { user ->
            user?.let {
                binding.tvWelcome.text = "Welcome, ${it.fullName ?: it.username}"
            }
        }
        
        // Observe dashboard state
        analyticsViewModel.dashboardState.observe(this) { state ->
            when {
                state.isLoading -> {
                    // Show loading indicators
                }
                state.error != null -> {
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                }
                state.userStats != null -> {
                    updateDashboardUI(state.userStats)
                }
            }
        }
        
        // Observe user stats
        analyticsViewModel.userStats.observe(this) { userStats ->
            userStats?.let {
                updateDashboardUI(it)
            }
        }
        
        // Observe error messages
        analyticsViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateDashboardUI(userStats: Any) {
        // TODO: Update this method to use correct layout IDs and data structure
        /*
        // Update safety score
        val safetyScore = userStats.safetyScore.toInt()
        safetyScoreText.text = "$safetyScore%"
        safetyScoreProgress.progress = safetyScore
        
        // Set color based on safety score
        val color = GraphUtils.getSafetyScoreColor(userStats.safetyScore)
        safetyScoreProgress.setIndicatorColor(color)
        
        // Update stats
        totalScansText.text = userStats.totalScans.toString()
        harassmentDetectedText.text = userStats.harassmentDetected.toString()
        deepfakesDetectedText.text = userStats.deepfakesDetected.toString()
        
        // Update last scan
        lastScanText.text = userStats.lastScan ?: "No scans yet"
        */
    }
    
    private fun loadDashboardData() {
        // Load user data if not already loaded
        if (authViewModel.currentUser.value == null) {
            authViewModel.refreshUserData()
        }
        
        // Load analytics data
        analyticsViewModel.loadDashboardData()
    }
    
    private fun showTextScanDialog() {
        // TODO: Fix dialog layout reference
        Toast.makeText(this, "Text scanning feature coming soon", Toast.LENGTH_SHORT).show()
        /*
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Scan Text for Harassment")
            .setMessage("Enter text to scan for cyber harassment:")
            .setView(R.layout.dialog_text_scan)
            .setPositiveButton("Scan") { dialog, _ ->
                val editText = (dialog as androidx.appcompat.app.AlertDialog)
                    .findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.scan_text_input)
                val text = editText?.text.toString().trim()
                
                if (text.isNotEmpty()) {
                    // TODO: Implement text scanning
                    Toast.makeText(this, "Scanning text: $text", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter text to scan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
        */
    }
    
    private fun showQuickScanOptions() {
        val options = arrayOf(
            "Scan Text",
            "Scan Image/Video",
            "Start Deep Scan",
            "View Analytics"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Quick Scan Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showTextScanDialog()
                    1 -> startActivity(Intent(this, DeepfakeActivity::class.java))
                    2 -> startActivity(Intent(this, DeepScanActivity::class.java))
                    3 -> startActivity(Intent(this, AnalyticsActivity::class.java))
                }
            }
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to dashboard
        analyticsViewModel.refreshData()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        
        // Start periodic refresh
        updateHandler.postDelayed(refreshRunnable, 10000)
    }
    
    override fun onPause() {
        super.onPause()
        // Stop periodic refresh
        updateHandler.removeCallbacks(refreshRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        try {
            unregisterReceiver(statsUpdateReceiver)
        } catch (e: Exception) {
            // Receiver already unregistered
        }
        updateHandler.removeCallbacks(refreshRunnable)
    }
    
    override fun onBackPressed() {
        // Show exit confirmation
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit DeepGuard")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
