package com.example.shieldx.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.shieldx.R
import com.example.shieldx.databinding.ActivityDeepfakeBinding
import com.example.shieldx.viewmodel.ScanViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class DeepfakeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeepfakeBinding
    private lateinit var scanViewModel: ScanViewModel
    private var selectedFileUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFileSelection(uri, "image")
            }
        }
    }
    
    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFileSelection(uri, "video")
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeepfakeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        scanViewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        
        setupUI()
        setupObservers()
        setupBottomNavigation()
    }
    
    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        
        binding.btnSelectImage.setOnClickListener {
            selectImage()
        }
        
        binding.btnSelectVideo.setOnClickListener {
            selectVideo()
        }
        
        binding.btnAnalyze.setOnClickListener {
            selectedFileUri?.let { uri ->
                startAnalysis(uri)
            }
        }
        
        binding.ivInfo.setOnClickListener {
            showInfoDialog()
        }
        
        binding.btnSaveReport.setOnClickListener {
            saveReport()
        }
        
        binding.btnShareReport.setOnClickListener {
            shareReport()
        }
    }
    
    private fun setupObservers() {
        scanViewModel.scanProgress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgressPercentage.text = "$progress%"
        }
        
        scanViewModel.isScanning.observe(this) { isScanning ->
            if (isScanning) {
                showProgressCard()
            } else {
                hideProgressCard()
            }
        }
        
        scanViewModel.scanError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                hideProgressCard()
            }
        }
        
        scanViewModel.scanResult.observe(this) { result ->
            result?.let {
                showResults(it)
            }
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_deepfake
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
                R.id.nav_deepfake -> true
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
    
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoPickerLauncher.launch(intent)
    }
    
    private fun handleFileSelection(uri: Uri, type: String) {
        selectedFileUri = uri
        
        // Show preview card
        binding.cardPreview.visibility = View.VISIBLE
        
        // Load image preview
        if (type == "image") {
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.ivPreview)
        } else {
            // For video, show a video thumbnail
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.ivPreview)
        }
        
        // Get file info
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
                
                if (nameIndex >= 0) {
                    val fileName = it.getString(nameIndex)
                    binding.tvFileName.text = fileName
                }
                
                if (sizeIndex >= 0) {
                    val fileSize = it.getLong(sizeIndex)
                    binding.tvFileSize.text = formatFileSize(fileSize)
                }
            }
        }
        
        // Enable analyze button
        binding.btnAnalyze.isEnabled = true
    }
    
    private fun startAnalysis(uri: Uri) {
        showProgressCard()
        scanViewModel.scanFile(uri, this)
    }
    
    private fun showProgressCard() {
        binding.cardProgress.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false
        binding.cardResults.visibility = View.GONE
    }
    
    private fun hideProgressCard() {
        binding.cardProgress.visibility = View.GONE
        binding.btnAnalyze.isEnabled = true
    }
    
    private fun showResults(result: com.example.shieldx.models.ScanResult) {
        binding.cardProgress.visibility = View.GONE
        binding.cardResults.visibility = View.VISIBLE
        
        // Update threat level
        binding.tvThreatLevel.text = when {
            result.isDeepfake -> "THREAT DETECTED"
            result.confidenceScore > 50 -> "SUSPICIOUS"
            else -> "SAFE"
        }
        
        // Update threat level color
        val threatColor = when {
            result.isDeepfake -> R.color.danger_color
            result.confidenceScore > 50 -> R.color.warning_color
            else -> R.color.success_color
        }
        binding.tvThreatLevel.setTextColor(getColor(threatColor))
        
        // Update confidence score
        binding.tvConfidenceScore.text = "${result.confidenceScore}%"
        
        // Update detailed analysis
        binding.tvDetailedAnalysis.text = result.detailedAnalysis ?: 
            "Analysis completed. The content has been scanned for deepfake indicators."
    }
    
    private fun showInfoDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Deepfake Detection")
            .setMessage("This feature uses advanced AI to detect deepfake content in images and videos. Upload a file to analyze its authenticity.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun saveReport() {
        // Implement save report functionality
        Toast.makeText(this, "Report saved", Toast.LENGTH_SHORT).show()
    }
    
    private fun shareReport() {
        // Implement share report functionality
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "DeepGuard Analysis Report")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Report"))
    }
    
    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes bytes"
        }
    }
}
