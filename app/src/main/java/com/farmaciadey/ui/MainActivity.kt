package com.farmaciadey.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.R
import com.farmaciadey.databinding.ActivityMainBinding
import com.farmaciadey.data.repository.CarritoRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.badge.BadgeDrawable
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: FarmaciaApplication
    private lateinit var carritoRepository: CarritoRepository
    private var carritoBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as FarmaciaApplication
        carritoRepository = CarritoRepository.getInstance()
        
        setupNavigation()
        checkAuthState()
        setupCarritoBadge()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        
        // Pantallas principales del bottom navigation
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_productos, 
                R.id.navigation_carrito, 
                R.id.navigation_perfil
            )
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Observar estado de autenticación
        lifecycleScope.launch {
            app.preferencesManager.tokenFlow.collect { token ->
                if (token.isNullOrEmpty()) {
                    // Usuario no autenticado, ir a login
                    navController.navigate(R.id.action_global_loginFragment)
                    navView.visibility = android.view.View.GONE
                } else {
                    // Usuario autenticado, mostrar bottom navigation
                    navView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
    
    private fun checkAuthState() {
        lifecycleScope.launch {
            val isLoggedIn = app.preferencesManager.isLoggedIn()
            if (!isLoggedIn) {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.action_global_loginFragment)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    private fun setupCarritoBadge() {
        // Crear badge para el carrito
        carritoBadge = binding.navView.getOrCreateBadge(R.id.navigation_carrito)
        carritoBadge?.isVisible = false
        
        // Observar cambios en el carrito
        lifecycleScope.launch {
            carritoRepository.getCarritoItemCount().collect { itemCount ->
                updateCarritoBadge(itemCount)
            }
        }
    }
    
    private fun updateCarritoBadge(itemCount: Int) {
        carritoBadge?.let { badge ->
            if (itemCount > 0) {
                badge.number = itemCount
                badge.isVisible = true
            } else {
                badge.isVisible = false
            }
        }
    }
}