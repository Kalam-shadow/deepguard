package com.example.shieldx.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.shieldx.utils.SharedPref

/**
 * DeepGuard v3.0 - Splash Activity
 * App startup screen with DeepGuard logo animation
 */
class SplashActivity : AppCompatActivity() {
    
    private lateinit var sharedPref: SharedPref
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.shieldx.R.layout.activity_splash)
        
        // Initialize SharedPreferences
        sharedPref = SharedPref.getInstance(this)
        
        // Initialize Lottie animation
        val animationView = findViewById<LottieAnimationView>(com.example.shieldx.R.id.lottie_animation)
        animationView.setAnimation(com.example.shieldx.R.raw.deepguard_logo_animation)
        animationView.playAnimation()
        
        // Navigate after animation delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 3000) // 3 second delay
    }
    
    private fun navigateToNextScreen() {
        val intent = if (sharedPref.isLoggedIn()) {
            // User is logged in, go to dashboard
            Intent(this, DashboardActivity::class.java)
        } else {
            // User not logged in, go to login
            Intent(this, LoginActivity::class.java)
        }
        
        startActivity(intent)
        finish()
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
