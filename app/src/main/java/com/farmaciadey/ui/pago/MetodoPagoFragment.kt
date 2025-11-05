package com.farmaciadey.ui.pago

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.farmaciadey.databinding.FragmentMetodoPagoBinding
import com.farmaciadey.data.models.MetodoPago
import com.farmaciadey.ui.viewmodel.PagoViewModel
import com.farmaciadey.ui.viewmodel.MetodoPagoViewModel
import com.farmaciadey.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import android.util.Log

class MetodoPagoFragment : Fragment() {
    
    private var _binding: FragmentMetodoPagoBinding? = null
    private val binding get() = _binding!!
    
    private val pagoViewModel: PagoViewModel by viewModels()
    private val metodoPagoViewModel: MetodoPagoViewModel by viewModels()
    private lateinit var metodoPagoAdapter: MetodoPagoAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMetodoPagoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupButtons()
    }
    
    private fun setupRecyclerView() {
        metodoPagoAdapter = MetodoPagoAdapter { metodo ->
            procederAlPago(metodo)
        }
        
        binding.recyclerMetodosPago.apply {
            adapter = metodoPagoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupObservers() {
        // Observar métodos de pago desde el microservicio
        lifecycleScope.launch {
            metodoPagoViewModel.uiState.collect { state ->
                Log.d("MetodoPagoFragment", "Estado recibido: isLoading=${state.isLoading}, metodos=${state.metodosPago.size}, error=${state.error}")
                
                // Mostrar/ocultar progress bar
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Mostrar error si existe
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                        .setAction("Reintentar") {
                            metodoPagoViewModel.recargar()
                        }
                        .show()
                }
                
                // Actualizar lista de métodos de pago
                if (state.metodosPago.isNotEmpty()) {
                    Log.d("MetodoPagoFragment", "Mostrando ${state.metodosPago.size} métodos de pago")
                    binding.recyclerMetodosPago.visibility = View.VISIBLE
                    binding.textViewNoMetodos?.visibility = View.GONE
                    metodoPagoAdapter.submitList(state.metodosPago)
                } else if (!state.isLoading && state.error == null) {
                    Log.d("MetodoPagoFragment", "No hay métodos de pago disponibles")
                    binding.recyclerMetodosPago.visibility = View.GONE
                    binding.textViewNoMetodos?.visibility = View.VISIBLE
                }
            }
        }
        
        // Observar el estado del pago
        lifecycleScope.launch {
            pagoViewModel.uiState.collect { state ->
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
                
                state.pagoResponse?.let { response ->
                    if (response.success) {
                        Snackbar.make(binding.root, "Pago procesado exitosamente", Snackbar.LENGTH_LONG).show()
                        // Navegar a pantalla de resultado
                    } else {
                        Snackbar.make(binding.root, "Error en el pago", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun setupButtons() {
        binding.buttonContinuar?.setOnClickListener {
            // Proceder al checkout o pantalla de confirmación
            Snackbar.make(binding.root, "Selecciona un método de pago", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun procederAlPago(metodo: MetodoPago) {
        // Obtener monto de los argumentos o usar valor por defecto
        val monto = arguments?.getDouble("monto", 100.0) ?: 100.0
        val descripcion = arguments?.getString("descripcion", "Pago Farmacia DeY") ?: "Pago Farmacia DeY"
        
        // Navegar según el tipo de método de pago
        when (metodo.tipo) {
            "Yape/Plin" -> {
                val fragment = YapePlinFragment().apply {
                    arguments = Bundle().apply {
                        putDouble("monto", monto)
                        putString("descripcion", descripcion)
                        putLong("metodoPagoId", metodo.id ?: 1L)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            "Visa" -> {
                val fragment = VisaFragment().apply {
                    arguments = Bundle().apply {
                        putDouble("monto", monto)
                        putString("descripcion", descripcion)
                        putLong("metodoPagoId", metodo.id ?: 3L)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            else -> {
                // Procesar pago directo para otros métodos
                metodo.id?.let { metodoPagoId ->
                    pagoViewModel.procesarPagoConSimulacion(metodoPagoId, monto)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
