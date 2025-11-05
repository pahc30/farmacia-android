package com.farmaciadey.ui.pago

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
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
import com.farmaciadey.data.models.CrearPagoRequest
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import kotlin.random.Random

class YapePlinFragment : Fragment() {
    
    private var _binding: FragmentYapePlinBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PagoViewModel by viewModels()
    private var codigoPago: String = ""
    private var monto: Double = 0.0
    private var metodoPagoId: Long = 1L // ID para Yape/Plin
    
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
        binding.textViewMonto?.text = "S/ %.2f".format(monto)
        binding.textViewDescripcion?.text = "Pago en Farmacia DeY"
        
        binding.buttonCopiarCodigo?.setOnClickListener {
            copiarCodigoAlPortapapeles()
        }
        
        binding.buttonConfirmarPago?.setOnClickListener {
            confirmarPago()
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
                binding.buttonConfirmarPago?.isEnabled = !state.isLoading
                
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
                
                state.pagoResponse?.let { response ->
                    if (response.success) {
                        // Pago exitoso - navegar a pantalla de boleta
                        binding.layoutQr?.visibility = View.GONE
                        binding.layoutSuccess?.visibility = View.VISIBLE
                        binding.textViewResultado?.text = "¡Pago Exitoso!"
                        binding.textViewTransaccionId?.text = "ID: ${response.transaccionId}"
                        
                        // Configurar botón para ir a boleta
                        binding.buttonVolverInicio?.text = "Ver Boleta"
                        binding.buttonVolverInicio?.setOnClickListener {
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
    
    private fun generarCodigoPago() {
        // Generar código aleatorio de 8 dígitos
        codigoPago = Random.nextInt(10000000, 99999999).toString()
        binding.textViewCodigo?.text = codigoPago
        
        // Generar QR
        generarQR(codigoPago)
    }
    
    private fun generarQR(codigo: String) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(codigo, BarcodeFormat.QR_CODE, 300, 300)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            binding.imageViewQr?.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            binding.imageViewQr?.visibility = View.GONE
            binding.textViewQrError?.visibility = View.VISIBLE
        }
    }
    
    private fun copiarCodigoAlPortapapeles() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Código de Pago", codigoPago)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.root, "Código copiado al portapapeles", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun confirmarPago() {
        // Procesar el pago con el código generado
        val request = CrearPagoRequest(
            compraId = 1L, // ID temporal de compra
            metodoPagoId = metodoPagoId,
            monto = monto,
            moneda = "PEN",
            descripcion = "Pago Yape/Plin - Farmacia DeY"
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
