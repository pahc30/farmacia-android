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
        
        // Cargar fragment de pagos
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MetodoPagoFragment())
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
