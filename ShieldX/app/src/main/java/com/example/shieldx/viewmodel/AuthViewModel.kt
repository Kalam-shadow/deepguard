package com.example.shieldx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.shieldx.models.*
import com.example.shieldx.repository.AuthRepository
import com.example.shieldx.network.ApiClient
import kotlinx.coroutines.launch

/**
 * DeepGuard v3.0 - Authentication ViewModel
 * Handles authentication state and operations
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application, ApiClient.getApiService())
    
    // Login state
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    // Signup state
    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState
    
    // Current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        // Load cached user data if logged in
        if (authRepository.isLoggedIn()) {
            _currentUser.value = authRepository.getCachedUser()
            refreshUserData()
        }
    }
    
    /**
     * Login user
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = LoginState(isLoading = true)
            
            try {
                val result = authRepository.login(username, password)
                
                result.fold(
                    onSuccess = { loginResponse ->
                        _currentUser.value = loginResponse.user
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = loginResponse.user,
                            message = "Login successful"
                        )
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _loginState.value = LoginState(
                            isError = true,
                            error = exception.message ?: "Login failed"
                        )
                        _errorMessage.value = exception.message ?: "Login failed"
                    }
                )
            } catch (e: Exception) {
                _loginState.value = LoginState(
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Signup new user
     */
    fun signup(username: String, email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _signupState.value = SignupState(isLoading = true)
            
            try {
                val result = authRepository.signup(username, email, password, fullName)
                
                result.fold(
                    onSuccess = { loginResponse ->
                        _currentUser.value = loginResponse.user
                        _signupState.value = SignupState(
                            isSuccess = true,
                            user = loginResponse.user,
                            message = "Account created successfully"
                        )
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _signupState.value = SignupState(
                            isError = true,
                            error = exception.message ?: "Signup failed"
                        )
                        _errorMessage.value = exception.message ?: "Signup failed"
                    }
                )
            } catch (e: Exception) {
                _signupState.value = SignupState(
                    isError = true,
                    error = e.message ?: "An unexpected error occurred"
                )
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh user data
     */
    fun refreshUserData() {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                    },
                    onFailure = { exception ->
                        // Handle silently or refresh token
                        refreshToken()
                    }
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Refresh authentication token
     */
    private fun refreshToken() {
        viewModelScope.launch {
            try {
                val result = authRepository.refreshToken()
                result.fold(
                    onSuccess = { loginResponse ->
                        _currentUser.value = loginResponse.user
                    },
                    onFailure = { exception ->
                        // Token refresh failed, logout user
                        logout()
                    }
                )
            } catch (e: Exception) {
                logout()
            }
        }
    }
    
    /**
     * Update user profile
     */
    fun updateProfile(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val result = authRepository.updateProfile(user)
                result.fold(
                    onSuccess = { updatedUser ->
                        _currentUser.value = updatedUser
                        _errorMessage.value = ""
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to update profile"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _loginState.value = LoginState()
        _signupState.value = SignupState()
        _errorMessage.value = ""
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = ""
    }
    
    /**
     * Clear login state
     */
    fun clearLoginState() {
        _loginState.value = LoginState()
    }
    
    /**
     * Clear signup state
     */
    fun clearSignupState() {
        _signupState.value = SignupState()
    }
}

/**
 * Login State Data Class
 */
data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val user: User? = null,
    val message: String = "",
    val error: String = ""
)

/**
 * Signup State Data Class
 */
data class SignupState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val user: User? = null,
    val message: String = "",
    val error: String = ""
)
