package com.farmaciadey.ui.pago

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.farmaciadey.R

class PagoActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)
        
        // Configurar toolbar
        supportActionBar?.apply {
            title = "Realizar Pago"
            setDisplayHomeAsUpEnabled(true)
        }
        
        // Obtener datos del intent
        val total = intent.getDoubleExtra("total", 0.0)
        val totalItems = intent.getIntExtra("totalItems", 0)
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        
        // Cargar fragment de pagos con los datos
        if (savedInstanceState == null) {
            val bundle = Bundle().apply {
                putDouble("total", total)
                putInt("totalItems", totalItems)
                putString("descripcion", descripcion)
            }
            
            val fragment = MetodoPagoFragment().apply {
                arguments = bundle
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
