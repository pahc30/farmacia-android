package com.farmaciadey

import android.app.Application
import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.utils.PreferencesManager

class FarmaciaApplication : Application() {
    
    lateinit var preferencesManager: PreferencesManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager(this)
        
        // Inicializar ApiClient
        ApiClient.init(preferencesManager)
    }
}