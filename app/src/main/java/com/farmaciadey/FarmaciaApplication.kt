package com.farmaciadey

import android.app.Application
import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.repository.CarritoRepository
import com.farmaciadey.utils.PreferencesManager

class FarmaciaApplication : Application() {
    
    lateinit var preferencesManager: PreferencesManager
        private set
        
    lateinit var carritoRepository: CarritoRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager(this)
        
        // Inicializar CarritoRepository (ahora sin persistencia)
        carritoRepository = CarritoRepository()
        
        // Inicializar ApiClient
        ApiClient.init(preferencesManager)
    }
}
