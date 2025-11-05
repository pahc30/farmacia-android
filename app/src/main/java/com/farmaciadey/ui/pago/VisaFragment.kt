package com.farmaciadey.ui.pago

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentVisaBinding
import com.farmaciadey.ui.viewmodel.PagoViewModel
import com.farmaciadey.data.models.CrearPagoRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class VisaFragment : Fragment() {
    
    private var _binding: FragmentVisaBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PagoViewModel by viewModels()
    private var monto: Double = 0.0
    private var metodoPagoId: Long = 3L // ID para Visa
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisaBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener datos del pago
        monto = arguments?.getDouble("monto") ?: 0.0
        metodoPagoId = arguments?.getLong("metodoPagoId") ?: 3L
        val descripcion = arguments?.getString("descripcion") ?: "Pago Farmacia DeY"
        
        setupUI()
        setupObservers()
        setupTextWatchers()
    }
    
    private fun setupUI() {
        binding.textViewMonto?.text = "Total a pagar: S/ %.2f".format(monto)
        
        binding.buttonPagar?.setOnClickListener {
            if (validarDatos()) {
                procesarPago()
            }
        }
        
        binding.buttonCancelar?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Mostrar/ocultar progress bar
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.buttonPagar?.isEnabled = !state.isLoading && validarDatos()
                
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
                
                state.pagoResponse?.let { response ->
                    if (response.success) {
                        // Pago exitoso - navegar a pantalla de boleta
                        binding.layoutFormulario?.visibility = View.GONE
                        binding.layoutSuccess?.visibility = View.VISIBLE
                        binding.textViewResultado?.text = "¡Pago Exitoso!"
                        binding.textViewTransaccionId?.text = "ID: ${response.transaccionId}"
                        binding.textViewMensajeExito?.text = response.message
                        
                        // Configurar botón para ir a boleta
                        binding.buttonVolverInicioVisa?.text = "Ver Boleta"
                        binding.buttonVolverInicioVisa?.setOnClickListener {
                            response.transaccionId?.let { transaccionId ->
                                navegarABoleta(transaccionId)
                            }
                        }
                    } else {
                        // Pago fallido
                        Snackbar.make(binding.root, response.message ?: "Error en el pago", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun setupTextWatchers() {
        // Formatear número de tarjeta (agregar espacios cada 4 dígitos)
        binding.editTextNumeroTarjeta?.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                
                isUpdating = true
                val formatted = formatCardNumber(s.toString())
                binding.editTextNumeroTarjeta?.setText(formatted)
                binding.editTextNumeroTarjeta?.setSelection(formatted.length)
                isUpdating = false
                
                // Validar y habilitar/deshabilitar botón
                binding.buttonPagar?.isEnabled = validarDatos()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Validar fecha de expiración (MM/YY)
        binding.editTextFechaExpiracion?.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                
                isUpdating = true
                val formatted = formatExpiryDate(s.toString())
                binding.editTextFechaExpiracion?.setText(formatted)
                binding.editTextFechaExpiracion?.setSelection(formatted.length)
                isUpdating = false
                
                binding.buttonPagar?.isEnabled = validarDatos()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Limitar CVV a 3-4 dígitos
        binding.editTextCvv?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonPagar?.isEnabled = validarDatos()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Validar nombre del titular
        binding.editTextNombreTitular?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonPagar?.isEnabled = validarDatos()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    private fun formatCardNumber(input: String): String {
        val digitsOnly = input.replace(" ", "").replace(Regex("[^\\d]"), "")
        val formatted = StringBuilder()
        
        for (i in digitsOnly.indices) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ")
            }
            if (i < 16) { // Limitar a 16 dígitos
                formatted.append(digitsOnly[i])
            }
        }
        
        return formatted.toString()
    }
    
    private fun formatExpiryDate(input: String): String {
        val digitsOnly = input.replace("/", "").replace(Regex("[^\\d]"), "")
        
        return when {
            digitsOnly.length >= 4 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2, 4)}"
            digitsOnly.length >= 2 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2)}"
            else -> digitsOnly
        }
    }
    
    private fun validarDatos(): Boolean {
        val numeroTarjeta = binding.editTextNumeroTarjeta?.text.toString()?.replace(" ", "") ?: ""
        val fechaExpiracion = binding.editTextFechaExpiracion?.text.toString() ?: ""
        val cvv = binding.editTextCvv?.text.toString() ?: ""
        val nombreTitular = binding.editTextNombreTitular?.text.toString()?.trim() ?: ""
        
        return numeroTarjeta.length == 16 &&
                fechaExpiracion.matches(Regex("\\d{2}/\\d{2}")) &&
                cvv.length in 3..4 &&
                nombreTitular.isNotBlank()
    }
    
    private fun procesarPago() {
        val numeroTarjeta = binding.editTextNumeroTarjeta?.text.toString()?.replace(" ", "") ?: ""
        val fechaExpiracion = binding.editTextFechaExpiracion?.text.toString() ?: ""
        val cvv = binding.editTextCvv?.text.toString() ?: ""
        val nombreTitular = binding.editTextNombreTitular?.text.toString()?.trim() ?: ""
        
        val request = CrearPagoRequest(
            compraId = 1L, // ID temporal de compra
            metodoPagoId = metodoPagoId,
            monto = monto,
            moneda = "PEN",
            descripcion = "Pago con Visa - Farmacia DeY"
        )
        
        viewModel.crearPago(request)
    }
    
    private fun navegarABoleta(transaccionId: Long) {
        // Crear el bundle con los argumentos
        val bundle = Bundle().apply {
            putLong("transaccionId", transaccionId)
        }
        
        // Crear y mostrar el BoletaFragment
        val boletaFragment = com.farmaciadey.ui.fragment.BoletaFragment().apply {
            arguments = bundle
        }
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, boletaFragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
