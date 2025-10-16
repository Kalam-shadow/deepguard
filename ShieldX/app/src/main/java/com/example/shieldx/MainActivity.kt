package com.example.shieldx

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
// Use the service helper for permission checks
import com.example.shieldx.api.ConnectionTester
import com.example.shieldx.utils.SharedPref
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    private var isWaitingForListenerPermission = false
    private lateinit var sharedPref: SharedPref
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkNotificationListenerPermission()
        } else {
            Toast.makeText(this, "Notification permission is required for ShieldX to work", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SharedPref
        sharedPref = SharedPref.getInstance(this)
        
        // Check if setup is already completed and user should go directly to login
        if (sharedPref.isPermissionSetupCompleted() && isNotificationListenerEnabled()) {
            redirectToLogin()
            return
        }
        
        setContent {
            ShieldXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShieldXMainScreen(
                        onRequestNotificationPermission = { requestNotificationPermission() },
                        onRequestListenerPermission = { requestNotificationListenerPermission() },
                        isNotificationListenerEnabled = { isNotificationListenerEnabled() },
                        onTestConnection = { testBackendConnection() },
                        onOpenDashboard = { openDashboard() },
                        onTestRedirect = { testRedirectToLogin() }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Debug logging
        android.util.Log.d("MainActivity", "onResume called - waiting: $isWaitingForListenerPermission, enabled: ${isNotificationListenerEnabled()}")
        android.util.Log.d("MainActivity", "Setup completed status: ${sharedPref.isPermissionSetupCompleted()}")
        
        // Check if user just returned from notification settings
        if (isWaitingForListenerPermission) {
            isWaitingForListenerPermission = false
            
            if (isNotificationListenerEnabled()) {
                // Permission granted! Mark setup as completed and redirect to login
                android.util.Log.d("MainActivity", "Permission granted! Setting setup completed and redirecting to login...")
                sharedPref.setPermissionSetupCompleted(true)
                android.util.Log.d("MainActivity", "Setup completed status after saving: ${sharedPref.isPermissionSetupCompleted()}")
                redirectToLogin()
            } else {
                android.util.Log.d("MainActivity", "Permission still not granted")
                Toast.makeText(
                    this,
                    "ShieldX needs notification access to protect you. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // Also check if permission was granted but we missed it (edge case)
            if (isNotificationListenerEnabled() && !sharedPref.isPermissionSetupCompleted()) {
                android.util.Log.d("MainActivity", "Permission already enabled but setup not marked complete. Fixing...")
                sharedPref.setPermissionSetupCompleted(true)
                redirectToLogin()
            }
        }
    }
    
    private fun redirectToLogin() {
        android.util.Log.d("MainActivity", "redirectToLogin() called - Bypassing login")
        
        // Mark user as logged in without actual authentication
        sharedPref.setLoggedIn(true)
        
        Toast.makeText(
            this,
            "🎉 Setup complete! Starting ShieldX directly (login bypassed)",
            Toast.LENGTH_LONG
        ).show()
        
        try {
            // Go directly to Dashboard instead of Login
            val intent = Intent(this, com.example.shieldx.activities.DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            android.util.Log.d("MainActivity", "Starting DashboardActivity directly...")
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error redirecting to login", e)
            Toast.makeText(this, "Error opening login screen", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            checkNotificationListenerPermission()
        }
    }
    
    private fun checkNotificationListenerPermission() {
        if (!isNotificationListenerEnabled()) {
            requestNotificationListenerPermission()
        }
    }
    
    private fun requestNotificationListenerPermission() {
        android.util.Log.d("MainActivity", "Requesting notification listener permission...")
        android.util.Log.d("MainActivity", "Current setup completed status: ${sharedPref.isPermissionSetupCompleted()}")
        isWaitingForListenerPermission = true
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
        Toast.makeText(
            this,
            "Please enable ShieldX in the Notification Access settings, then return to ShieldX",
            Toast.LENGTH_LONG
        ).show()
    }
    
    private fun isNotificationListenerEnabled(): Boolean {
        // Delegate to the service helper which uses the same check method used by the Service
        return com.example.shieldx.services.NotificationListenerService.isNotificationServiceEnabled(this)
    }
    
    private fun testBackendConnection() {
        ConnectionTester.runAllTests()
        Toast.makeText(this, "Connection test started - check Logcat for results", Toast.LENGTH_SHORT).show()
    }
    
    private fun openDashboard() {
        val intent = Intent(this, com.example.shieldx.activities.DashboardActivity::class.java)
        startActivity(intent)
    }
    
    private fun testRedirectToLogin() {
        android.util.Log.d("MainActivity", "Testing redirect to login...")
        android.util.Log.d("MainActivity", "Current listener status: ${isNotificationListenerEnabled()}")
        android.util.Log.d("MainActivity", "Current setup status: ${sharedPref.isPermissionSetupCompleted()}")
        
        if (isNotificationListenerEnabled()) {
            sharedPref.setPermissionSetupCompleted(true)
            redirectToLogin()
        } else {
            Toast.makeText(this, "Please enable notification listener first", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ShieldXMainScreen(
    onRequestNotificationPermission: () -> Unit,
    onRequestListenerPermission: () -> Unit,
    isNotificationListenerEnabled: () -> Boolean,
    onTestConnection: () -> Unit,
    onOpenDashboard: () -> Unit,
    onTestRedirect: () -> Unit
) {
    var listenerEnabled by remember { mutableStateOf(isNotificationListenerEnabled()) }
    var isConnected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Automatically refresh listener status when the composable is displayed
    LaunchedEffect(Unit) {
        listenerEnabled = isNotificationListenerEnabled()
        android.util.Log.d("MainActivity", "LaunchedEffect - Initial listener status: $listenerEnabled")
    }
    
    // Listen for lifecycle changes and refresh when resuming
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val previousState = listenerEnabled
                listenerEnabled = isNotificationListenerEnabled()
                android.util.Log.d("MainActivity", "Lifecycle ON_RESUME - Previous: $previousState, Current: $listenerEnabled")
                
                // If permission was just granted, trigger redirect
                if (!previousState && listenerEnabled) {
                    android.util.Log.d("MainActivity", "Permission detected via lifecycle! Triggering redirect...")
                    onTestRedirect()
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon and Title
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Shield",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "🛡️ ShieldX",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "AI-Powered Harassment Detection",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Status Cards
        StatusCard(
            title = if (listenerEnabled) "✅ Protection Active" else "⚠️ Setup Required",
            description = if (listenerEnabled) 
                "ShieldX is monitoring your notifications for harassment" 
            else 
                "Notification access permission is required",
            isActive = listenerEnabled
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StatusCard(
            title = if (isConnected) "🌐 Backend Connected" else "🔴 Backend Disconnected",
            description = if (isConnected) 
                "Connected to DeepGuard AI backend" 
            else 
                "Unable to reach harassment detection backend",
            isActive = isConnected
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        if (!listenerEnabled) {
            Button(
                onClick = onRequestNotificationPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🛡️ Enable Protection & Continue to Login")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "After enabling notification access, you'll be redirected to login",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            // Show main app entry button when setup is complete
            Button(
                onClick = onOpenDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀 Open ShieldX Dashboard")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        OutlinedButton(
            onClick = { 
                val previousState = listenerEnabled
                listenerEnabled = isNotificationListenerEnabled()
                android.util.Log.d("MainActivity", "Manual refresh - Previous: $previousState, Current: $listenerEnabled")
                
                // If permission was just granted, trigger redirect
                if (!previousState && listenerEnabled) {
                    android.util.Log.d("MainActivity", "Permission detected via manual refresh! Triggering redirect...")
                    onTestRedirect()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 Refresh Status & Check Permission")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = {
                scope.launch {
                    onTestConnection()
                    // Simulate connection test result
                    kotlinx.coroutines.delay(2000)
                    isConnected = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Backend Connection")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = { 
                // First refresh the UI state, then test redirect
                val previousState = listenerEnabled
                listenerEnabled = isNotificationListenerEnabled()
                android.util.Log.d("MainActivity", "Test button - Previous: $previousState, Current: $listenerEnabled")
                
                if (listenerEnabled) {
                    onTestRedirect()
                } else {
                    android.util.Log.d("MainActivity", "Test button - Permission not enabled yet")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧪 Test Login Redirect (Debug)")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📱 Monitored Apps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "WhatsApp • Messenger • Instagram • Telegram • SMS • Snapchat",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    description: String,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Security else Icons.Default.Warning,
                contentDescription = if (isActive) "Active" else "Inactive",
                tint = if (isActive) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isActive) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ShieldXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            primaryContainer = Color(0xFFE3F2FD),
            secondary = Color(0xFF00BCD4),
            error = Color(0xFFD32F2F),
            errorContainer = Color(0xFFFFEBEE)
        )
    ) {
        content()
    }
}
