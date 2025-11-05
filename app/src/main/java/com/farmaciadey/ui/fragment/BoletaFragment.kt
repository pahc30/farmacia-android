package com.farmaciadey.ui.fragment

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentBoletaBinding
import com.farmaciadey.ui.viewmodel.BoletaViewModel
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar y descargar boleta de pago
 */
class BoletaFragment : Fragment() {

    private var _binding: FragmentBoletaBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: BoletaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoletaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[BoletaViewModel::class.java]
        
        setupUI()
        setupObservers()
        
        // Cargar datos de la transacción
        arguments?.getLong("transaccionId")?.let { transaccionId ->
            viewModel.cargarDatosTransaccion(transaccionId)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Botón para descargar boleta
            btnDescargarBoleta.setOnClickListener {
                arguments?.getLong("transaccionId")?.let { transaccionId ->
                    descargarBoleta(transaccionId)
                }
            }
            
            // Botón para ver historial
            btnVerHistorial.setOnClickListener {
                try {
                    findNavController().navigate(R.id.action_global_historial_compras)
                } catch (e: Exception) {
                    navegarAHistorial()
                }
            }
            
            // Botón de volver - regresa a productos
            btnVolver.setOnClickListener {
                navegarAProductos()
            }
        }
    }
    
    private fun navegarAHistorial() {
        val historialFragment = HistorialComprasFragment()
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, historialFragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun navegarAProductos() {
        // Estrategia múltiple para asegurar que regrese a productos
        try {
            // Opción 1: Intentar navegar directamente a productos
            findNavController().navigate(R.id.navigation_productos)
        } catch (e1: Exception) {
            try {
                // Opción 2: Limpiar el stack y navegar a productos
                findNavController().popBackStack(R.id.navigation_productos, false)
            } catch (e2: Exception) {
                try {
                    // Opción 3: Navegar hacia atrás hasta llegar al inicio
                    while (findNavController().navigateUp()) {
                        // Continuar navegando hacia atrás
                    }
                } catch (e3: Exception) {
                    // Opción 4: Fallback usando el activity
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    
                    state.transaccionData?.let { data ->
                        tvTransaccionId.text = "Transacción: ${data.transaccionId}"
                        tvMonto.text = "Monto: S/ %.2f".format(data.monto)
                        tvMetodoPago.text = "Método: ${data.metodoPago}"
                        tvFecha.text = "Fecha: ${data.fecha}"
                        tvEstado.text = "Estado: ${data.estado}"
                    }
                    
                    state.error?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun descargarBoleta(transaccionId: Long) {
        try {
            val baseUrl = "http://10.0.2.2:9000"
            val downloadUrl = "$baseUrl/metodopago/api/v1/pagos/boleta/transaccion/$transaccionId"
            
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Boleta de Pago")
                setDescription("Descargando boleta de transacción $transaccionId")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "boleta_$transaccionId.pdf")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }
            
            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            Toast.makeText(context, "Iniciando descarga de boleta...", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error al descargar boleta: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}