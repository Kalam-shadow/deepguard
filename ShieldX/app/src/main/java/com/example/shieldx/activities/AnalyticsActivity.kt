package com.example.shieldx.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shieldx.R
import com.example.shieldx.adapters.DetectionAdapter
import com.example.shieldx.data.AnalyticsData
import com.example.shieldx.data.ScanActivityData
import com.example.shieldx.data.AccuracyData
import com.example.shieldx.databinding.ActivityAnalyticsBinding
import com.example.shieldx.models.Detection
import com.example.shieldx.viewmodel.AnalyticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class AnalyticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var analyticsViewModel: AnalyticsViewModel
    private lateinit var detectionAdapter: DetectionAdapter
    private val detections = mutableListOf<Detection>()
    private val updateHandler = Handler(Looper.getMainLooper())
    
    private var selectedTimePeriod = "7days"
    
    // BroadcastReceiver to listen for real-time threat detection updates
    private val statsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.shieldx.STATS_UPDATED") {
                // Refresh analytics data when threats are detected
                loadAnalyticsData()
            }
        }
    }
    
    // Auto-refresh runnable for periodic updates
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadAnalyticsData()
            updateHandler.postDelayed(this, 15000) // Refresh every 15 seconds
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        analyticsViewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        
        // Register broadcast receiver for real-time updates
        val filter = IntentFilter("com.example.shieldx.STATS_UPDATED")
        registerReceiver(statsUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        
        setupUI()
        setupCharts()
        setupRecyclerView()
        setupObservers()
        setupBottomNavigation()
        
        // Load initial data
        loadAnalyticsData()
        
        // Start periodic refresh
        updateHandler.postDelayed(refreshRunnable, 15000)
    }
    
    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        
        binding.ivExport.setOnClickListener {
            exportAnalytics()
        }
        
        binding.tvViewAll.setOnClickListener {
            // Show all detections
            viewAllDetections()
        }
        
        // Setup time period chip selection
        binding.chipGroupTimePeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chip7Days -> selectedTimePeriod = "7days"
                    R.id.chip30Days -> selectedTimePeriod = "30days"
                    R.id.chip3Months -> selectedTimePeriod = "3months"
                }
                loadAnalyticsData()
            }
        }
    }
    
    private fun setupCharts() {
        setupLineChart()
        setupPieChart()
        setupBarChart()
    }
    
    private fun setupLineChart() {
        binding.chartScanActivity.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(Color.TRANSPARENT)
            
            // X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
                setDrawGridLines(false)
                granularity = 1f
            }
            
            // Y-axis (left)
            axisLeft.apply {
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.dark_gray)
            }
            
            // Y-axis (right)
            axisRight.isEnabled = false
            
            // Legend
            legend.textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
        }
    }
    
    private fun setupPieChart() {
        binding.chartThreatTypes.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            
            setDrawHoleEnabled(true)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 61f
            
            setDrawCenterText(true)
            centerText = "Threat\nTypes"
            setCenterTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.white))
            setCenterTextSize(16f)
            
            setRotationAngle(0f)
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            legend.apply {
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
                textSize = 12f
            }
        }
    }
    
    private fun setupBarChart() {
        binding.chartDetectionAccuracy.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setBackgroundColor(Color.TRANSPARENT)
            
            // X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
                setDrawGridLines(false)
                granularity = 1f
            }
            
            // Y-axis (left)
            axisLeft.apply {
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.dark_gray)
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            // Y-axis (right)
            axisRight.isEnabled = false
            
            // Legend
            legend.textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
        }
    }
    
    private fun setupRecyclerView() {
        detectionAdapter = DetectionAdapter(detections) { detection ->
            showDetectionDetails(detection)
        }
        
        binding.rvRecentDetections.apply {
            layoutManager = LinearLayoutManager(this@AnalyticsActivity)
            adapter = detectionAdapter
        }
    }
    
    private fun setupObservers() {
        analyticsViewModel.analyticsData.observe(this) { data ->
            data?.let {
                updateOverviewStats(it)
                updateScanActivityChart(it.scanActivity)
                updateThreatTypesChart(it.threatTypes)
                updateDetectionAccuracyChart(it.detectionAccuracy)
            }
        }
        
        analyticsViewModel.recentDetections.observe(this) { detectionList ->
            detectionList?.let {
                detections.clear()
                detections.addAll(it)
                detectionAdapter.notifyDataSetChanged()
                
                binding.tvNoDetections.visibility = if (detections.isEmpty()) View.VISIBLE else View.GONE
                binding.rvRecentDetections.visibility = if (detections.isEmpty()) View.GONE else View.VISIBLE
            }
        }
        
        analyticsViewModel.isLoading.observe(this) { isLoading ->
            // Show/hide loading indicator if needed
        }
        
        analyticsViewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_analytics
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
                R.id.nav_analytics -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadAnalyticsData() {
        analyticsViewModel.loadAnalyticsData(selectedTimePeriod)
        analyticsViewModel.loadRecentDetections()
    }
    
    private fun updateOverviewStats(data: AnalyticsData) {
        binding.tvOverviewTotalScans.text = data.totalScans.toString()
        binding.tvOverviewThreats.text = data.threatsFound.toString()
        
        val successRate = if (data.totalScans > 0) {
            ((data.totalScans - data.threatsFound) * 100 / data.totalScans)
        } else {
            100
        }
        binding.tvOverviewSuccessRate.text = "${successRate}%"
    }
    
    private fun updateScanActivityChart(scanActivity: List<ScanActivityData>) {
        val entries = scanActivity.mapIndexed { index, point ->
            Entry(index.toFloat(), point.scans.toFloat())
        }
        
        val dataSet = LineDataSet(entries, "Daily Scans").apply {
            color = ContextCompat.getColor(this@AnalyticsActivity, R.color.primary_color)
            setCircleColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.primary_color))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
            setDrawValues(false)
        }
        
        val lineData = LineData(dataSet)
        binding.chartScanActivity.data = lineData
        
        // Set x-axis labels
        val labels = scanActivity.map { it.date }
        binding.chartScanActivity.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        binding.chartScanActivity.invalidate()
    }
    
    private fun updateThreatTypesChart(threatTypes: Map<String, Int>) {
        val entries = threatTypes.map { (type, count) ->
            PieEntry(count.toFloat(), type)
        }
        
        val dataSet = PieDataSet(entries, "Threat Types").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        
        val pieData = PieData(dataSet)
        binding.chartThreatTypes.data = pieData
        binding.chartThreatTypes.invalidate()
    }
    
    private fun updateDetectionAccuracyChart(accuracyData: List<com.example.shieldx.data.AccuracyData>) {
        val entries = accuracyData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.accuracy.toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Detection Accuracy").apply {
            color = ContextCompat.getColor(this@AnalyticsActivity, R.color.primary_color)
            valueTextColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.light_gray)
            valueTextSize = 12f
        }
        
        val barData = BarData(dataSet)
        binding.chartDetectionAccuracy.data = barData
        
        // Set x-axis labels
        val labels = accuracyData.map { it.date }
        binding.chartDetectionAccuracy.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        binding.chartDetectionAccuracy.invalidate()
    }
    
    private fun exportAnalytics() {
        // Implement analytics export functionality
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "DeepGuard Analytics Report - $selectedTimePeriod")
            putExtra(Intent.EXTRA_SUBJECT, "DeepGuard Analytics Report")
        }
        startActivity(Intent.createChooser(intent, "Export Analytics"))
    }
    
    private fun viewAllDetections() {
        // Navigate to detailed detections view
        // This could be implemented as a new activity or fragment
    }
    
    private fun showDetectionDetails(detection: Detection) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detection Details")
            .setMessage("""
                Type: ${detection.type}
                Confidence: ${detection.confidence}%
                Source: ${detection.source}
                Time: ${detection.timestamp}
                ${detection.details ?: ""}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showError(error: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        loadAnalyticsData()
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
}
