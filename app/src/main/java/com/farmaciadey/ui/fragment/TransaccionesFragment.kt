package com.farmaciadey.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentTransaccionesBinding
import com.farmaciadey.databinding.ItemTransaccionBinding
import com.farmaciadey.data.models.TransaccionPago
import com.farmaciadey.data.models.EstadoPago
import com.farmaciadey.ui.viewmodel.TransaccionesViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment para mostrar el historial de transacciones de una compra
 */
class TransaccionesFragment : Fragment() {
    
    private var _binding: FragmentTransaccionesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TransaccionesViewModel by viewModels()
    private lateinit var adapter: TransaccionesAdapter
    private var compraId: Long = 0L
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransaccionesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        compraId = arguments?.getLong("compraId") ?: 0L
        
        if (compraId == 0L) {
            Snackbar.make(binding.root, "Error: ID de compra no válido", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }
        
        setupUI()
        setupObservers()
        
        // Cargar transacciones
        viewModel.cargarTransacciones(compraId)
    }
    
    private fun setupUI() {
        binding.textViewTitulo?.text = "Transacciones de Compra #$compraId"
        
        // Configurar RecyclerView
        adapter = TransaccionesAdapter { transaccion ->
            onTransaccionClick(transaccion)
        }
        
        binding.recyclerViewTransacciones?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransaccionesFragment.adapter
        }
        
        // Botón para actualizar
        binding.buttonActualizar?.setOnClickListener {
            viewModel.cargarTransacciones(compraId)
        }
        
        // Botón volver
        binding.buttonVolver?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        // Swipe to refresh
        binding.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.cargarTransacciones(compraId)
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Mostrar/ocultar loading
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.swipeRefreshLayout?.isRefreshing = state.isLoading
                
                // Actualizar lista de transacciones
                if (state.transacciones.isNotEmpty()) {
                    binding.recyclerViewTransacciones?.visibility = View.VISIBLE
                    binding.textViewEmpty?.visibility = View.GONE
                    adapter.submitList(state.transacciones)
                    
                    // Mostrar resumen
                    mostrarResumen(state.transacciones)
                } else if (!state.isLoading) {
                    binding.recyclerViewTransacciones?.visibility = View.GONE
                    binding.textViewEmpty?.visibility = View.VISIBLE
                }
                
                // Mostrar errores
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    viewModel.limpiarError()
                }
            }
        }
    }
    
    private fun mostrarResumen(transacciones: List<TransaccionPago>) {
        val totalTransacciones = transacciones.size
        val completadas = transacciones.count { it.estado == EstadoPago.COMPLETADA }
        val pendientes = transacciones.count { it.estado == EstadoPago.PENDIENTE }
        val fallidas = transacciones.count { it.estado == EstadoPago.FALLIDA }
        val montoTotal = transacciones
            .filter { it.estado == EstadoPago.COMPLETADA }
            .sumOf { it.monto }
        
        binding.textViewResumen?.text = """
            Total: $totalTransacciones transacciones
            ✅ Completadas: $completadas
            ⏳ Pendientes: $pendientes
            ❌ Fallidas: $fallidas
            💰 Monto total pagado: S/ %.2f
        """.trimIndent().format(montoTotal)
    }
    
    private fun onTransaccionClick(transaccion: TransaccionPago) {
        // Navegar a detalle de boleta si la transacción está completada
        if (transaccion.estado == EstadoPago.COMPLETADA && transaccion.id != null) {
            val bundle = Bundle().apply {
                putLong("transaccionId", transaccion.id)
                putLong("compraId", compraId)
            }
            
            val boletaFragment = BoletaFragment().apply {
                arguments = bundle
            }
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, boletaFragment)
                .addToBackStack(null)
                .commit()
        } else {
            Snackbar.make(
                binding.root,
                "Transacción en estado: ${transaccion.estado}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(compraId: Long): TransaccionesFragment {
            return TransaccionesFragment().apply {
                arguments = Bundle().apply {
                    putLong("compraId", compraId)
                }
            }
        }
    }
}

/**
 * Adapter para mostrar la lista de transacciones
 */
class TransaccionesAdapter(
    private val onTransaccionClick: (TransaccionPago) -> Unit
) : RecyclerView.Adapter<TransaccionesAdapter.TransaccionViewHolder>() {
    
    private var transacciones: List<TransaccionPago> = emptyList()
    
    fun submitList(nuevasTransacciones: List<TransaccionPago>) {
        transacciones = nuevasTransacciones
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val binding = ItemTransaccionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransaccionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        holder.bind(transacciones[position], onTransaccionClick)
    }
    
    override fun getItemCount(): Int = transacciones.size
    
    class TransaccionViewHolder(
        private val binding: ItemTransaccionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(transaccion: TransaccionPago, onClick: (TransaccionPago) -> Unit) {
            binding.textViewTransaccionId?.text = "ID: ${transaccion.id ?: "N/A"}"
            binding.textViewMonto?.text = "S/ %.2f".format(transaccion.monto)
            binding.textViewEstado?.text = getEstadoTexto(transaccion.estado)
            binding.textViewFecha?.text = formatearFecha(transaccion.fechaCreacion)
            binding.textViewDescripcion?.text = transaccion.descripcion ?: "Sin descripción"
            binding.textViewMetodoPago?.text = getMetodoPagoTexto(transaccion.metodoPagoId)
            
            // Color según estado
            val colorEstado = when (transaccion.estado) {
                EstadoPago.COMPLETADA -> android.graphics.Color.parseColor("#4CAF50")
                EstadoPago.PENDIENTE -> android.graphics.Color.parseColor("#FF9800")
                EstadoPago.PROCESANDO -> android.graphics.Color.parseColor("#2196F3")
                EstadoPago.FALLIDA -> android.graphics.Color.parseColor("#F44336")
                EstadoPago.CANCELADA -> android.graphics.Color.parseColor("#9E9E9E")
                EstadoPago.REEMBOLSADA -> android.graphics.Color.parseColor("#9C27B0")
            }
            binding.textViewEstado?.setTextColor(colorEstado)
            
            // Click listener
            binding.root.setOnClickListener {
                onClick(transaccion)
            }
            
            // Mostrar referencia externa si existe
            if (transaccion.referenciaExterna != null) {
                binding.textViewReferencia?.visibility = View.VISIBLE
                binding.textViewReferencia?.text = "Ref: ${transaccion.referenciaExterna}"
            } else {
                binding.textViewReferencia?.visibility = View.GONE
            }
        }
        
        private fun getEstadoTexto(estado: EstadoPago): String {
            return when (estado) {
                EstadoPago.COMPLETADA -> "✅ COMPLETADA"
                EstadoPago.PENDIENTE -> "⏳ PENDIENTE"
                EstadoPago.PROCESANDO -> "🔄 PROCESANDO"
                EstadoPago.FALLIDA -> "❌ FALLIDA"
                EstadoPago.CANCELADA -> "🚫 CANCELADA"
                EstadoPago.REEMBOLSADA -> "↩️ REEMBOLSADA"
            }
        }
        
        private fun getMetodoPagoTexto(metodoPagoId: Long): String {
            return when (metodoPagoId) {
                1L -> "💳 Yape/Plin"
                2L -> "💳 Yape/Plin"
                3L -> "💳 Visa"
                else -> "💳 Método #$metodoPagoId"
            }
        }
        
        private fun formatearFecha(fecha: String?): String {
            if (fecha == null) return "Fecha no disponible"
            
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(fecha)
                date?.let { outputFormat.format(it) } ?: fecha
            } catch (e: Exception) {
                fecha
            }
        }
    }
}
