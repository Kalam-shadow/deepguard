package com.example.shieldx.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.shieldx.R
import com.example.shieldx.utils.SharedPref
import com.example.shieldx.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * DeepGuard v3.0 - Login Activity
 * User authentication screen with Material Design
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var authViewModel: AuthViewModel
    
    // UI Components
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signupButton: MaterialButton
    private lateinit var forgotPasswordButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // BYPASS LOGIN: Automatically go to dashboard without showing login screen
        val sharedPref = SharedPref.getInstance(this)
        sharedPref.setLoggedIn(true)
        
        Toast.makeText(
            this, 
            "🛡️ Login bypassed - Starting ShieldX directly", 
            Toast.LENGTH_SHORT
        ).show()
        
        // Go directly to Dashboard
        val dashboardIntent = Intent(this, DashboardActivity::class.java)
        dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(dashboardIntent)
        finish()
        
        // The code below won't execute due to the finish() call above
        setContentView(R.layout.activity_login)
        
        // Initialize ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Initialize UI
        initializeViews()
        setupClickListeners()
        observeViewModel()
        
        // Check if already logged in
        if (authViewModel.isLoggedIn()) {
            navigateToDashboard()
        }
    }
    
    private fun initializeViews() {
        emailInputLayout = findViewById(R.id.email_input_layout)
        passwordInputLayout = findViewById(R.id.password_input_layout)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById(R.id.login_button)
        signupButton = findViewById(R.id.signup_button)
        forgotPasswordButton = findViewById(R.id.forgot_password_button)
        progressIndicator = findViewById(R.id.progress_indicator)
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            attemptLogin()
        }
        
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        forgotPasswordButton.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        // Observe login state
        authViewModel.loginState.observe(this) { loginState ->
            when {
                loginState.isLoading -> {
                    showLoading(true)
                }
                loginState.isSuccess -> {
                    showLoading(false)
                    Toast.makeText(this, loginState.message, Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                }
                loginState.isError -> {
                    showLoading(false)
                    showError(loginState.error)
                }
            }
        }
        
        // Observe loading state
        authViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
        
        // Observe error messages
        authViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                showError(error)
            }
        }
    }
    
    private fun attemptLogin() {
        // Clear previous errors
        emailInputLayout.error = null
        passwordInputLayout.error = null
        authViewModel.clearError()
        
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        // Validate input
        if (!validateInput(email, password)) {
            return
        }
        
        // Attempt login
        authViewModel.login(email, password)
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Please enter a valid email"
            isValid = false
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        return isValid
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.text = "Signing In..."
        } else {
            progressIndicator.visibility = View.GONE
            loginButton.isEnabled = true
            loginButton.text = "Sign In"
        }
    }
    
    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
