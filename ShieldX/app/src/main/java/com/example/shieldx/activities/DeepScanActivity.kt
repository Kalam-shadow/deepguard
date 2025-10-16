package com.example.shieldx.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shieldx.R
import com.example.shieldx.adapters.AlertAdapter
import com.example.shieldx.databinding.ActivityDeepscanBinding
import com.example.shieldx.models.Alert
import com.example.shieldx.services.NotificationListenerService
import com.example.shieldx.utils.SharedPref
import com.example.shieldx.viewmodel.ScanViewModel
import java.text.SimpleDateFormat
import java.util.*

class DeepScanActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeepscanBinding
    private lateinit var scanViewModel: ScanViewModel
    private lateinit var sharedPref: SharedPref
    private lateinit var alertAdapter: AlertAdapter
    private val alerts = mutableListOf<Alert>()
    
    private var isMonitoring = false
    private val monitoringHandler = Handler(Looper.getMainLooper())
    private var monitoringStartTime = 0L
    private var monitoringDuration = 0L
    
    // BroadcastReceiver to listen for real-time threat detection updates
    private val statsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.shieldx.STATS_UPDATED") {
                // Update statistics when new threats are detected
                val scanned = intent.getIntExtra("notifications_scanned", 0)
                val detected = intent.getIntExtra("threats_detected", 0)
                val blocked = intent.getIntExtra("threats_blocked", 0)
                val warnings = intent.getIntExtra("warnings_sent", 0)
                
                // Update UI with real-time stats
                binding.tvNotificationsScanned.text = scanned.toString()
                binding.tvThreatsBlocked.text = blocked.toString()
                binding.tvWarningsSent.text = warnings.toString()
                
                // Reload alerts to show new threats
                scanViewModel.loadRecentAlerts()
            }
        }
    }
    
    private val monitoringRunnable = object : Runnable {
        override fun run() {
            updateMonitoringStats()
            
            // Periodically refresh alerts and stats while monitoring
            if (isMonitoring) {
                scanViewModel.loadMonitoringStats()
                scanViewModel.loadRecentAlerts()
            }
            
            monitoringHandler.postDelayed(this, 5000) // Update every 5 seconds
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeepscanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        scanViewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        sharedPref = SharedPref.getInstance(this)
        
        // Register broadcast receiver for real-time updates
        val filter = IntentFilter("com.example.shieldx.STATS_UPDATED")
        registerReceiver(statsUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupBottomNavigation()
        loadSettings()
    }
    
    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        
        binding.switchNotificationMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startMonitoring()
            } else {
                stopMonitoring()
            }
        }
        
        binding.switchRealTimeMode.setOnCheckedChangeListener { _, isChecked ->
            // Handle real-time mode toggle
            sharedPref.setRealTimeModeEnabled(isChecked)
        }
        
        binding.switchHarassmentDetection.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setHarassmentDetectionEnabled(isChecked)
        }
        
        binding.switchDeepfakeDetection.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setDeepfakeDetectionEnabled(isChecked)
        }
        
        binding.switchAutoBlock.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setAutoBlockEnabled(isChecked)
        }
        
        binding.switchRealTimeMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.setRealTimeModeEnabled(isChecked)
            if (isChecked) {
                showRealTimeModeSettings()
            }
        }
        
        binding.btnStartMonitoring.setOnClickListener {
            startMonitoring()
        }
        
        binding.btnStopMonitoring.setOnClickListener {
            stopMonitoring()
        }
    }
    
    private fun setupRecyclerView() {
        alertAdapter = AlertAdapter(alerts) { alert ->
            // Handle alert click
            showAlertDetails(alert)
        }
        
        binding.rvRecentAlerts.apply {
            layoutManager = LinearLayoutManager(this@DeepScanActivity)
            adapter = alertAdapter
        }
    }
    
    private fun setupObservers() {
        scanViewModel.monitoringStats.observe(this) { stats ->
            stats?.let {
                binding.tvNotificationsScanned.text = it.notificationsScanned.toString()
                binding.tvThreatsBlocked.text = it.threatsBlocked.toString()
                binding.tvWarningsSent.text = it.warningsSent.toString()
            }
        }
        
        scanViewModel.recentAlerts.observe(this) { alertList ->
            alertList?.let {
                alerts.clear()
                alerts.addAll(it)
                alertAdapter.notifyDataSetChanged()
                
                binding.tvNoAlerts.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
                binding.rvRecentAlerts.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_deepscan
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_deepscan -> true
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
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadSettings() {
        binding.switchRealTimeMode.isChecked = sharedPref.isRealTimeModeEnabled()
        binding.switchHarassmentDetection.isChecked = sharedPref.isHarassmentDetectionEnabled()
        binding.switchDeepfakeDetection.isChecked = sharedPref.isDeepfakeDetectionEnabled()
        binding.switchAutoBlock.isChecked = sharedPref.isAutoBlockEnabled()
        
        // Load monitoring state
        isMonitoring = sharedPref.isMonitoringActive()
        binding.switchNotificationMonitoring.isChecked = isMonitoring
        
        if (isMonitoring) {
            showMonitoringActive()
            startMonitoringTimer()
        }
    }
    
    private fun startMonitoring() {
        if (!NotificationListenerService.isNotificationServiceEnabled(this)) {
            showNotificationAccessDialog()
            return
        }
        
        isMonitoring = true
        monitoringStartTime = System.currentTimeMillis()
        sharedPref.setMonitoringActive(true)
        sharedPref.setMonitoringStartTime(monitoringStartTime)
        
        showMonitoringActive()
        startMonitoringTimer()
        
        // Start the notification listener service
        val serviceIntent = Intent(this, NotificationListenerService::class.java)
        startService(serviceIntent)
        
        // Load recent alerts and stats
        scanViewModel.loadMonitoringStats()
        scanViewModel.loadRecentAlerts()
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        sharedPref.setMonitoringActive(false)
        
        showMonitoringStopped()
        stopMonitoringTimer()
        
        // Stop the notification listener service
        val serviceIntent = Intent(this, NotificationListenerService::class.java)
        stopService(serviceIntent)
    }
    
    private fun showMonitoringActive() {
        binding.cardMonitoringStatus.visibility = View.VISIBLE
        binding.btnStartMonitoring.visibility = View.GONE
        binding.btnStopMonitoring.visibility = View.VISIBLE
        binding.switchNotificationMonitoring.isChecked = true
        
        binding.statusIndicator.backgroundTintList = getColorStateList(R.color.success_color)
    }
    
    private fun showMonitoringStopped() {
        binding.cardMonitoringStatus.visibility = View.GONE
        binding.btnStartMonitoring.visibility = View.VISIBLE
        binding.btnStopMonitoring.visibility = View.GONE
        binding.switchNotificationMonitoring.isChecked = false
    }
    
    private fun startMonitoringTimer() {
        monitoringHandler.post(monitoringRunnable)
    }
    
    private fun stopMonitoringTimer() {
        monitoringHandler.removeCallbacks(monitoringRunnable)
    }
    
    private fun updateMonitoringStats() {
        if (isMonitoring) {
            val currentTime = System.currentTimeMillis()
            monitoringDuration = currentTime - monitoringStartTime
            
            val hours = (monitoringDuration / (1000 * 60 * 60)) % 24
            val minutes = (monitoringDuration / (1000 * 60)) % 60
            val seconds = (monitoringDuration / 1000) % 60
            
            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            binding.tvMonitoringDuration.text = timeString
        }
    }
    
    private fun showNotificationAccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("DeepGuard needs notification access to monitor incoming messages for threats. Please grant this permission in the next screen.")
            .setPositiveButton("Grant Access") { _, _ ->
                NotificationListenerService.openNotificationAccessSettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAlertDetails(alert: Alert) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Alert Details")
            .setMessage("""
                App: ${alert.appName}
                Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))}
                Threat Type: ${alert.threatType}
                Confidence: ${alert.confidence}%
                Content: ${alert.content}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("Block Sender") { _, _ ->
                // Implement block sender functionality
            }
            .create()
        
        dialog.show()
    }
    
    private fun showRealTimeModeSettings() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Real-time Mode Settings")
            .setMessage("""
                Real-time mode monitors content in real-time for:
                - Deepfake detection
                - Harassment detection
                - Content analysis
                
                This mode may impact battery life and performance.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .create()
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoringTimer()
        
        // Unregister broadcast receiver
        try {
            unregisterReceiver(statsUpdateReceiver)
        } catch (e: Exception) {
            // Receiver already unregistered
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        if (isMonitoring) {
            scanViewModel.loadMonitoringStats()
            scanViewModel.loadRecentAlerts()
        }
    }
}
