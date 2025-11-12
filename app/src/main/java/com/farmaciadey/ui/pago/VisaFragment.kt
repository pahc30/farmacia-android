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
import com.farmaciadey.ui.carrito.CarritoViewModel
import com.farmaciadey.ui.carrito.CarritoViewModelFactory
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.CrearPagoRequest
import com.farmaciadey.data.models.DatosTarjeta
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Fragment para pagos con tarjeta Visa
 * Implementa el flujo completo de PaymentIntent y confirmación
 */
class VisaFragment : Fragment() {
    
    private var _binding: FragmentVisaBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PagoViewModel by viewModels()
    private val carritoViewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory(requireActivity().application as FarmaciaApplication)
    }
    private var monto: Double = 0.0
    private var metodoPagoId: Long = 2L // ID para Visa
    private var compraId: Long = 0L // Se asigna cuando se crea la compra
    
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
        metodoPagoId = arguments?.getLong("metodoPagoId") ?: 2L
        val descripcion = arguments?.getString("descripcion") ?: "Pago Farmacia DeY"
        
        setupUI()
        setupObservers()
        setupTextWatchers()
    }
    
    private fun setupUI() {
        binding.textViewMonto?.text = "Total a pagar: S/ %.2f".format(monto)
        binding.textViewDescripcion?.text = "Ingresa los datos de tu tarjeta Visa"
        
        binding.buttonPagar?.setOnClickListener {
            if (validarDatos()) {
                procesarPago()
            }
        }
        
        binding.buttonCancelar?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        // Mostrar íconos de seguridad
        binding.textViewSeguridad?.text = "🔒 Pago seguro cifrado"
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Mostrar/ocultar progress bar
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.buttonPagar?.isEnabled = !state.isLoading && validarDatos()
                
                // Deshabilitar campos mientras se procesa
                setFormEnabled(!state.isLoading)
                
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
                
                state.pagoResponse?.let { response ->
                    if (response.success) {
                        // Pago exitoso
                        mostrarPagoExitoso(response.transaccionId, response.referenciaExterna)
                    } else {
                        // Pago fallido
                        mostrarPagoFallido(response.message ?: "Error procesando el pago")
                    }
                }
            }
        }
    }
    
    private fun setFormEnabled(enabled: Boolean) {
        binding.editTextNumeroTarjeta?.isEnabled = enabled
        binding.editTextFechaExpiracion?.isEnabled = enabled
        binding.editTextCvv?.isEnabled = enabled
        binding.editTextNombreTitular?.isEnabled = enabled
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
                
                // Validar y actualizar botón
                actualizarEstadoBoton()
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
                
                actualizarEstadoBoton()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Limitar CVV a 3-4 dígitos
        binding.editTextCvv?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                actualizarEstadoBoton()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Validar nombre del titular
        binding.editTextNombreTitular?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                actualizarEstadoBoton()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    private fun actualizarEstadoBoton() {
        binding.buttonPagar?.isEnabled = validarDatos()
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
        
        // Validación básica
        val numeroValido = numeroTarjeta.length == 16
        val fechaValida = fechaExpiracion.matches(Regex("\\d{2}/\\d{2}"))
        val cvvValido = cvv.length in 3..4
        val nombreValido = nombreTitular.isNotBlank() && nombreTitular.length >= 3
        
        // Mostrar errores si es necesario
        if (!numeroValido && numeroTarjeta.isNotEmpty()) {
            binding.editTextNumeroTarjeta?.error = "Número de tarjeta inválido"
        }
        if (!fechaValida && fechaExpiracion.isNotEmpty()) {
            binding.editTextFechaExpiracion?.error = "Formato: MM/YY"
        }
        if (!cvvValido && cvv.isNotEmpty()) {
            binding.editTextCvv?.error = "CVV debe tener 3 o 4 dígitos"
        }
        if (!nombreValido && nombreTitular.isNotEmpty()) {
            binding.editTextNombreTitular?.error = "Nombre inválido"
        }
        
        return numeroValido && fechaValida && cvvValido && nombreValido
    }
    
    /**
     * Procesa el pago creando un PaymentIntent
     */
    private fun procesarPago() {
        val numeroTarjeta = binding.editTextNumeroTarjeta?.text.toString()?.replace(" ", "") ?: ""
        val fechaExpiracion = binding.editTextFechaExpiracion?.text.toString() ?: ""
        val cvv = binding.editTextCvv?.text.toString() ?: ""
        val nombreTitular = binding.editTextNombreTitular?.text.toString()?.trim() ?: ""
        
        lifecycleScope.launch {
            try {
                // Paso 1: Crear la compra con el método de pago Visa
                val resultadoCompra = carritoViewModel.crearCompraDesdeCarrito(metodoPagoId.toInt())
                
                resultadoCompra.fold(
                    onSuccess = { compraIdCreado ->
                        // Guardar el ID de compra creado para usarlo en navegarABoleta
                        compraId = compraIdCreado.toLong()
                        
                        // IMPORTANTE: Recargar carrito porque el backend lo vació al crear la compra
                        carritoViewModel.limpiarCarritoLocal()
                        
                        // Paso 2: Crear datos de tarjeta
                        val datosTarjeta = DatosTarjeta(
                            numeroTarjeta = numeroTarjeta,
                            fechaExpiracion = fechaExpiracion,
                            cvv = cvv,
                            nombreTitular = nombreTitular
                        )
                        
                        // Paso 3: Crear request de pago con el ID de compra real
                        val request = CrearPagoRequest(
                            compraId = compraId,
                            metodoPagoId = metodoPagoId,
                            monto = monto,
                            moneda = "PEN",
                            descripcion = "Pago con Visa - Farmacia DeY",
                            datosTarjeta = datosTarjeta
                        )
                        
                        // Paso 4: Procesar pago a través del ViewModel
                        viewModel.crearPago(request)
                    },
                    onFailure = { error ->
                        Snackbar.make(binding.root, "Error al crear compra: ${error.message}", Snackbar.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra la pantalla de pago exitoso
     */
    private fun mostrarPagoExitoso(transaccionId: Long?, referenciaExterna: String?) {
        // Ocultar formulario
        binding.layoutFormulario?.visibility = View.GONE
        
        // Mostrar layout de éxito
        binding.layoutSuccess?.visibility = View.VISIBLE
        binding.textViewResultado?.text = "¡Pago Exitoso!"
        binding.textViewTransaccionId?.text = "Transacción #${transaccionId ?: "N/A"}"
        binding.textViewReferenciaExterna?.text = "Ref: ${referenciaExterna ?: "N/A"}"
        binding.textViewMensajeExito?.text = """
            Tu pago con Visa ha sido procesado exitosamente.
            
            Monto: S/ %.2f
            
            Puedes descargar tu boleta a continuación.
        """.trimIndent().format(monto)
        
        // Botón para ver boleta
        binding.buttonVerBoleta?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                transaccionId?.let { id ->
                    navegarABoleta(id)
                }
            }
        }
        
        // Botón para volver al inicio
        binding.buttonVolverInicioVisa?.apply {
            visibility = View.VISIBLE
            text = "Volver al Inicio"
            setOnClickListener {
                requireActivity().finish()
            }
        }
    }
    
    /**
     * Muestra mensaje de pago fallido
     */
    private fun mostrarPagoFallido(mensaje: String) {
        Snackbar.make(
            binding.root,
            "❌ $mensaje",
            Snackbar.LENGTH_LONG
        ).apply {
            setAction("Reintentar") {
                // Limpiar campos y reiniciar
                limpiarFormulario()
            }
            show()
        }
    }
    
    /**
     * Limpia el formulario para un nuevo intento
     */
    private fun limpiarFormulario() {
        binding.editTextNumeroTarjeta?.text?.clear()
        binding.editTextFechaExpiracion?.text?.clear()
        binding.editTextCvv?.text?.clear()
        binding.editTextNombreTitular?.text?.clear()
        binding.editTextNumeroTarjeta?.requestFocus()
    }
    
    /**
     * Navega al fragment de boleta
     */
    private fun navegarABoleta(transaccionId: Long) {
        val bundle = Bundle().apply {
            putLong("transaccionId", transaccionId)
            putLong("compraId", compraId)
        }
        
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
    
    companion object {
        /**
         * Factory method para crear el fragment con los parámetros necesarios
         * El compraId ya no se pasa como argumento, se crea cuando se procesa el pago
         */
        fun newInstance(monto: Double, metodoPagoId: Long): VisaFragment {
            return VisaFragment().apply {
                arguments = Bundle().apply {
                    putDouble("monto", monto)
                    putLong("metodoPagoId", metodoPagoId)
                }
            }
        }
    }
}
