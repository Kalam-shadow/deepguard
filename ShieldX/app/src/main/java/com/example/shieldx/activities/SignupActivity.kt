package com.example.shieldx.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.shieldx.R
import com.example.shieldx.activities.DashboardActivity
import com.example.shieldx.utils.SharedPref
import com.example.shieldx.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.ProgressBar

/**
 * DeepGuard v3.0 - Signup Activity
 * User registration screen with validation
 */
class SignupActivity : AppCompatActivity() {
    
    private lateinit var authViewModel: AuthViewModel
    
    // UI Components
    private lateinit var fullNameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    
    private lateinit var signupButton: MaterialButton
    private lateinit var loginNavText: TextView
    private lateinit var progressIndicator: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // BYPASS SIGNUP: Automatically go to dashboard without showing signup screen
        val sharedPref = SharedPref.getInstance(this)
        sharedPref.setLoggedIn(true)
        
        Toast.makeText(
            this, 
            "🛡️ Signup bypassed - Starting ShieldX directly", 
            Toast.LENGTH_SHORT
        ).show()
        
        // Go directly to Dashboard
        navigateToDashboard()
        
        // The code below won't execute due to the navigateToDashboard() call above
        setContentView(R.layout.activity_signup)
        
        // Initialize ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Initialize UI
        initializeViews()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun initializeViews() {
        fullNameInputLayout = findViewById(R.id.tilFullName)
        emailInputLayout = findViewById(R.id.tilEmail)
        passwordInputLayout = findViewById(R.id.tilPassword)
        confirmPasswordInputLayout = findViewById(R.id.tilConfirmPassword)
        
        fullNameEditText = findViewById(R.id.etFullName)
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        
        signupButton = findViewById(R.id.btnSignUp)
        loginNavText = findViewById(R.id.tvAlreadyHaveAccount)
        progressIndicator = findViewById(R.id.progressBar)
    }
    
    private fun setupClickListeners() {
        signupButton.setOnClickListener {
            attemptSignup()
        }
        
        loginNavText.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
    
    private fun observeViewModel() {
        // Observe signup state
        authViewModel.signupState.observe(this) { signupState ->
            when {
                signupState.isLoading -> {
                    showLoading(true)
                }
                signupState.isSuccess -> {
                    showLoading(false)
                    Toast.makeText(this, signupState.message, Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                }
                signupState.isError -> {
                    showLoading(false)
                    showError(signupState.error)
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
    
    private fun attemptSignup() {
        // Clear previous errors
        clearErrors()
        authViewModel.clearError()
        
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        
        // Validate input
        if (!validateInput(fullName, email, password, confirmPassword)) {
            return
        }
        
        // Attempt signup - use email as username
        authViewModel.signup(email, email, password, fullName)
    }
    
    private fun validateInput(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        
        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            fullNameInputLayout.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            fullNameInputLayout.error = "Full name must be at least 2 characters"
            isValid = false
        }
        
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
        } else if (!isPasswordStrong(password)) {
            passwordInputLayout.error = "Password must contain at least one letter and one number"
            isValid = false
        }
        
        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }
        
        return isValid
    }
    
    private fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
    
    private fun clearErrors() {
        fullNameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
            signupButton.isEnabled = false
            signupButton.text = "Creating Account..."
        } else {
            progressIndicator.visibility = View.GONE
            signupButton.isEnabled = true
            signupButton.text = "Create Account"
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
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
