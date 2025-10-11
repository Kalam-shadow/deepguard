package com.example.shieldx

import android.app.Application
import androidx.multidex.MultiDexApplication

class ShieldXApplication : MultiDexApplication() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any global configurations here
    }
}