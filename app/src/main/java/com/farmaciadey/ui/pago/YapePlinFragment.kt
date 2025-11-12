package com.farmaciadey.ui.pago

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentYapePlinBinding
import com.farmaciadey.ui.viewmodel.PagoViewModel
import com.farmaciadey.ui.carrito.CarritoViewModel
import com.farmaciadey.ui.carrito.CarritoViewModelFactory
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.CrearPagoRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Fragment para pagos con Yape/Plin usando código para copiar
 * No usa QR, solo código numérico que el usuario puede copiar
 */
class YapePlinFragment : Fragment() {
    
    private var _binding: FragmentYapePlinBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PagoViewModel by viewModels()
    private val carritoViewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory(requireActivity().application as FarmaciaApplication)
    }
    private var codigoPago: String = ""
    private var monto: Double = 0.0
    private var metodoPagoId: Long = 1L // ID para Yape/Plin
    private var compraId: Long = 0L // Se asigna cuando se crea la compra
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYapePlinBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener datos del pago
        monto = arguments?.getDouble("monto") ?: 0.0
        metodoPagoId = arguments?.getLong("metodoPagoId") ?: 1L
        val descripcion = arguments?.getString("descripcion") ?: "Pago Farmacia DeY"
        
        setupUI()
        setupObservers()
        generarCodigoPago()
    }
    
    private fun setupUI() {
        // Configurar información del pago
        binding.textViewMonto?.text = "S/ %.2f".format(monto)
        binding.textViewDescripcion?.text = "Pago en Farmacia DeY"
        binding.textViewInstrucciones?.text = """
            Instrucciones:
            1. Abre tu app de Yape o Plin
            2. Selecciona "Pagar a comercio"
            3. Ingresa o pega el código mostrado abajo
            4. Confirma el monto: S/ %.2f
            5. Completa el pago
            6. Presiona "Ya pagué" en esta pantalla
        """.trimIndent().format(monto)
        
        // Ocultar el QR si existe en el layout
        binding.imageViewQr?.visibility = View.GONE
        binding.textViewQrError?.visibility = View.GONE
        
        // Botón para copiar código
        binding.buttonCopiarCodigo?.setOnClickListener {
            copiarCodigoAlPortapapeles()
        }
        
        // Botón para confirmar que ya pagó
        binding.buttonConfirmarPago?.apply {
            text = "Ya pagué"
            setOnClickListener {
                confirmarPago()
            }
        }
        
        // Botón cancelar
        binding.buttonCancelar?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Mostrar/ocultar progress bar
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.buttonConfirmarPago?.isEnabled = !state.isLoading
                binding.buttonCopiarCodigo?.isEnabled = !state.isLoading
                
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
                
                state.pagoResponse?.let { response ->
                    if (response.success) {
                        // Pago exitoso - mostrar confirmación
                        mostrarPagoExitoso(response.transaccionId)
                    } else {
                        // Pago fallido
                        Snackbar.make(
                            binding.root, 
                            response.message ?: "Error en el pago. Por favor intenta nuevamente.", 
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
    
    /**
     * Genera un código de pago aleatorio de 8 dígitos
     */
    private fun generarCodigoPago() {
        // Generar código aleatorio de 8 dígitos
        codigoPago = Random.nextInt(10000000, 99999999).toString()
        
        // Mostrar el código en formato legible (XXXX-XXXX)
        val codigoFormateado = "${codigoPago.substring(0, 4)}-${codigoPago.substring(4)}"
        binding.textViewCodigo?.apply {
            text = codigoFormateado
            textSize = 32f
            // Hacer el código seleccionable
            setTextIsSelectable(true)
        }
    }
    
    /**
     * Copia el código de pago al portapapeles
     */
    private fun copiarCodigoAlPortapapeles() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Código de Pago Yape/Plin", codigoPago)
        clipboard.setPrimaryClip(clip)
        
        Snackbar.make(
            binding.root, 
            "✓ Código copiado: $codigoPago", 
            Snackbar.LENGTH_SHORT
        ).show()
        
        // Cambiar el texto del botón temporalmente
        binding.buttonCopiarCodigo?.apply {
            val textoOriginal = text
            text = "✓ Copiado"
            postDelayed({
                text = textoOriginal
            }, 2000)
        }
    }
    
    /**
     * Confirma el pago en el backend
     */
    private fun confirmarPago() {
        lifecycleScope.launch {
            try {
                // Mostrar loading
                binding.progressBar?.visibility = View.VISIBLE
                binding.buttonConfirmarPago?.isEnabled = false
                
                // Paso 1: Crear la compra con el método de pago seleccionado
                val resultadoCompra = carritoViewModel.crearCompraDesdeCarrito(metodoPagoId.toInt())
                
                resultadoCompra.fold(
                    onSuccess = { compraIdCreado ->
                        // Guardar el ID de compra creado para usarlo en navegarABoleta
                        compraId = compraIdCreado.toLong()
                        
                        // IMPORTANTE: Recargar carrito porque el backend lo vació al crear la compra
                        carritoViewModel.limpiarCarritoLocal()
                        
                        // Paso 2: Crear el request de pago con el ID de compra real
                        val request = CrearPagoRequest(
                            compraId = compraId,
                            metodoPagoId = metodoPagoId,
                            monto = monto,
                            moneda = "PEN",
                            descripcion = "Pago Yape/Plin - Código: $codigoPago",
                            codigoPago = codigoPago
                        )
                        
                        // Paso 3: Procesar el pago
                        viewModel.crearPago(request)
                    },
                    onFailure = { error ->
                        binding.progressBar?.visibility = View.GONE
                        binding.buttonConfirmarPago?.isEnabled = true
                        Snackbar.make(binding.root, "Error al crear compra: ${error.message}", Snackbar.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                binding.progressBar?.visibility = View.GONE
                binding.buttonConfirmarPago?.isEnabled = true
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra la pantalla de pago exitoso
     */
    private fun mostrarPagoExitoso(transaccionId: Long?) {
        // Ocultar el layout de pago
        binding.layoutQr?.visibility = View.GONE
        
        // Mostrar el layout de éxito
        binding.layoutSuccess?.visibility = View.VISIBLE
        binding.textViewResultado?.text = "¡Pago Exitoso!"
        binding.textViewTransaccionId?.text = "Transacción #${transaccionId ?: "N/A"}"
        binding.textViewMensajeExito?.text = """
            Tu pago con Yape/Plin ha sido procesado exitosamente.
            
            Código de pago: $codigoPago
            Monto: S/ %.2f
            
            Puedes descargar tu boleta a continuación.
        """.trimIndent().format(monto)
        
        // Configurar botón para ver boleta
        binding.buttonVerBoleta?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                transaccionId?.let { id ->
                    navegarABoleta(id)
                }
            }
        }
        
        // Configurar botón para volver al inicio
        binding.buttonVolverInicio?.apply {
            visibility = View.VISIBLE
            text = "Volver al Inicio"
            setOnClickListener {
                // Regresar a la pantalla principal
                requireActivity().finish()
            }
        }
    }
    
    /**
     * Navega al fragment de boleta para descargar el PDF
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
         * El compraId ya no se pasa como argumento, se crea cuando se confirma el pago
         */
        fun newInstance(monto: Double, metodoPagoId: Long): YapePlinFragment {
            return YapePlinFragment().apply {
                arguments = Bundle().apply {
                    putDouble("monto", monto)
                    putLong("metodoPagoId", metodoPagoId)
                }
            }
        }
    }
}
