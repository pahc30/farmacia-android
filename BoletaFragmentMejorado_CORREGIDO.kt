package com.farmaciadey.ui.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentBoletaBinding
import com.farmaciadey.ui.viewmodel.BoletaViewModelMejorado
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Fragment mejorado para mostrar y descargar boletas de pago en PDF
 * Soporta descarga por transacción y por compra
 */
class BoletaFragmentMejorado : Fragment() {

    private var _binding: FragmentBoletaBinding? = null
    private val binding get() = _binding!!
    
    // CORRECCIÓN 1: Usar BoletaViewModelMejorado en lugar de BoletaViewModel
    private val viewModel: BoletaViewModelMejorado by viewModels()
    
    private var transaccionId: Long? = null
    private var compraId: Long? = null
    
    // Launcher para solicitar permisos de escritura
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            iniciarDescarga()
        } else {
            Snackbar.make(
                binding.root,
                "Permiso de almacenamiento necesario para descargar",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

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
        
        // Obtener argumentos
        transaccionId = arguments?.getLong("transaccionId")?.takeIf { it > 0 }
        compraId = arguments?.getLong("compraId")?.takeIf { it > 0 }
        
        setupUI()
        setupObservers()
        cargarDatos()
    }
    
    private fun setupUI() {
        binding.apply {
            // CORRECCIÓN 2: Remover elvis operator (?.) si los elementos existen en el layout
            // O agregar safe calls si son opcionales
            textViewTitulo.text = when {
                transaccionId != null -> "Boleta de Transacción #$transaccionId"
                compraId != null -> "Boleta de Compra #$compraId"
                else -> "Boleta de Pago"
            }
            
            // Botón para descargar boleta por transacción
            buttonDescargarTransaccion.apply {
                visibility = if (transaccionId != null) View.VISIBLE else View.GONE
                setOnClickListener {
                    verificarPermisosYDescargar()
                }
            }
            
            // Botón para descargar boleta por compra
            buttonDescargarCompra.apply {
                visibility = if (compraId != null) View.VISIBLE else View.GONE
                setOnClickListener {
                    verificarPermisosYDescargar()
                }
            }
            
            // Botón para ver transacciones
            buttonVerTransacciones.apply {
                visibility = if (compraId != null) View.VISIBLE else View.GONE
                setOnClickListener {
                    navegarATransacciones()
                }
            }
            
            // Botón de volver
            buttonVolver.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Mostrar/ocultar loading
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.buttonDescargarTransaccion.isEnabled = !state.isLoading
                binding.buttonDescargarCompra.isEnabled = !state.isLoading
                
                // CORRECCIÓN 3: Verificar si existe el método cargarDatosTransaccion
                // Si no existe, usar solo los métodos de descarga
                
                // Manejar descarga exitosa
                state.boletaPdf?.let { pdfBytes ->
                    guardarYAbrirPDF(pdfBytes)
                    viewModel.limpiarBoleta()
                }
                
                // Mostrar errores
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    viewModel.limpiarError()
                }
            }
        }
    }
    
    private fun cargarDatos() {
        // CORRECCIÓN 4: Comentado si no existe el método
        // transaccionId?.let { id ->
        //     viewModel.cargarDatosTransaccion(id)
        // }
    }
    
    /**
     * Verifica permisos y descarga la boleta
     */
    private fun verificarPermisosYDescargar() {
        // En Android 10+ no se necesita permiso de escritura para Downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            iniciarDescarga()
        } else {
            // Para Android 9 y anteriores, verificar permiso
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    iniciarDescarga()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }
    
    /**
     * Inicia la descarga de la boleta
     */
    private fun iniciarDescarga() {
        when {
            transaccionId != null -> viewModel.descargarBoletaTransaccion(transaccionId!!)
            compraId != null -> viewModel.descargarBoletaCompra(compraId!!)
            else -> {
                Snackbar.make(
                    binding.root,
                    "Error: No hay ID de transacción o compra",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Guarda el PDF en el dispositivo y lo abre
     */
    private fun guardarYAbrirPDF(pdfBytes: ByteArray) {
        try {
            val fileName = when {
                transaccionId != null -> "boleta_transaccion_$transaccionId.pdf"
                compraId != null -> "boleta_compra_$compraId.pdf"
                else -> "boleta_${System.currentTimeMillis()}.pdf"
            }
            
            // CORRECCIÓN 5: Usar getExternalFilesDir para Android 10+
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Usar app-specific directory
                val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                File(dir, fileName)
            } else {
                // Android 9-: Usar public Downloads
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                File(downloadsDir, fileName)
            }
            
            // Guardar el PDF
            FileOutputStream(file).use { fos ->
                fos.write(pdfBytes)
            }
            
            Snackbar.make(
                binding.root,
                "✓ Boleta descargada: ${file.name}",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("Abrir") {
                    abrirPDF(file)
                }
                show()
            }
            
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "Error al guardar PDF: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Abre el PDF con una aplicación externa
     */
    private fun abrirPDF(file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "No se encontró aplicación para abrir PDF",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Navega al fragment de transacciones
     */
    private fun navegarATransacciones() {
        compraId?.let { id ->
            val bundle = Bundle().apply {
                putLong("compraId", id)
            }
            
            val transaccionesFragment = TransaccionesFragment().apply {
                arguments = bundle
            }
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, transaccionesFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        /**
         * Factory method para crear por transacción
         */
        fun newInstancePorTransaccion(transaccionId: Long, compraId: Long? = null): 
            BoletaFragmentMejorado {
            return BoletaFragmentMejorado().apply {
                arguments = Bundle().apply {
                    putLong("transaccionId", transaccionId)
                    compraId?.let { putLong("compraId", it) }
                }
            }
        }
        
        /**
         * Factory method para crear por compra
         */
        fun newInstancePorCompra(compraId: Long): BoletaFragmentMejorado {
            return BoletaFragmentMejorado().apply {
                arguments = Bundle().apply {
                    putLong("compraId", compraId)
                }
            }
        }
    }
}
