package com.farmaciadey.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentHistorialComprasBinding
import com.farmaciadey.ui.adapter.HistorialComprasAdapter
import com.farmaciadey.ui.viewmodel.HistorialComprasViewModel
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar el historial de compras del usuario
 */
class HistorialComprasFragment : Fragment() {

    private var _binding: FragmentHistorialComprasBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HistorialComprasViewModel
    private lateinit var adapter: HistorialComprasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialComprasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[HistorialComprasViewModel::class.java]
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupUI()
        setupObservers()
        
        // Cargar historial
        viewModel.cargarHistorialCompras()
    }
    
    private fun setupRecyclerView() {
        adapter = HistorialComprasAdapter { compra ->
            // Navegar a detalles de la compra o descargar boleta
            navegarABoleta(compra.transaccionId)
        }
        
        binding.recyclerViewHistorial.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HistorialComprasFragment.adapter
            
            // DEBUG: Hacer visible el RecyclerView con fondo
            setBackgroundColor(android.graphics.Color.parseColor("#FFCCCC"))
            Log.d("HistorialFragment", "RecyclerView setup con adapter de ${this@HistorialComprasFragment.adapter.itemCount} items")
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                viewModel.cargarHistorialCompras()
            }
            
            // Configurar colores del SwipeRefreshLayout
            setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
        }
    }
    
    private fun navegarABoleta(transaccionId: Long) {
        try {
            val bundle = Bundle().apply {
                putLong("transaccionId", transaccionId)
            }
            findNavController().navigate(R.id.action_global_boleta, bundle)
        } catch (e: Exception) {
            // Fallback usando FragmentManager
            val boletaFragment = BoletaFragment().apply {
                arguments = Bundle().apply {
                    putLong("transaccionId", transaccionId)
                }
            }
            
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, boletaFragment)
                .addToBackStack("boleta")
                .commit()
        }
    }
    
    private fun setupUI() {
        binding.apply {
            btnVolver.setOnClickListener {
                // Usar navigateUp para regresar al fragmento anterior
                if (!findNavController().navigateUp()) {
                    // Si no puede navegar hacia atrás, ir a perfil
                    findNavController().navigate(R.id.navigation_perfil)
                }
            }
        }
    }
    
    private fun setupObservers() {
        Log.d("HistorialFragment", "setupObservers: Iniciando observación del StateFlow")
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                Log.d("HistorialFragment", "StateFlow recibido - isLoading: ${state.isLoading}, compras: ${state.compras.size}, error: ${state.error}")
                
                binding.apply {
                    // Controlar el loading del SwipeRefreshLayout
                    swipeRefreshLayout.isRefreshing = state.isLoading
                    
                    // Mostrar progress bar solo para carga inicial (cuando no hay datos)
                    progressBar.visibility = if (state.isLoading && adapter.itemCount == 0) {
                        Log.d("HistorialFragment", "Mostrando ProgressBar")
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    
                    // Actualizar la lista
                    if (state.compras.isNotEmpty()) {
                        Log.d("HistorialFragment", "Mostrando ${state.compras.size} compras en RecyclerView")
                        tvEmptyState.visibility = View.GONE
                        recyclerViewHistorial.visibility = View.VISIBLE
                        
                        // Forzar nueva lista para que DiffUtil detecte cambios
                        val newList = state.compras.toList()
                        adapter.submitList(newList) {
                            // Callback después de que la lista se actualiza
                            Log.d("HistorialFragment", "submitList completado. Adapter tiene ${adapter.itemCount} items")
                            Log.d("HistorialFragment", "RecyclerView visibility: ${recyclerViewHistorial.visibility}")
                            Log.d("HistorialFragment", "RecyclerView childCount: ${recyclerViewHistorial.childCount}")
                            
                            // Forzar el RecyclerView a medir y dibujar
                            recyclerViewHistorial.requestLayout()
                            recyclerViewHistorial.invalidate()
                            
                            // Log post-layout
                            recyclerViewHistorial.post {
                                Log.d("HistorialFragment", "POST-layout childCount: ${recyclerViewHistorial.childCount}")
                                Log.d("HistorialFragment", "POST-layout height: ${recyclerViewHistorial.height}")
                                Log.d("HistorialFragment", "POST-layout adapter: ${recyclerViewHistorial.adapter?.itemCount}")
                            }
                        }
                        Log.d("HistorialFragment", "Lista enviada al adapter, itemCount después: ${adapter.itemCount}")
                    } else if (!state.isLoading) {
                        Log.d("HistorialFragment", "Mostrando empty state - no hay compras")
                        recyclerViewHistorial.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                    }
                    
                    // Mostrar errores
                    state.error?.let { error ->
                        Log.e("HistorialFragment", "Error recibido: $error")
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
