package com.farmaciadey.ui.productos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.databinding.FragmentProductosBinding
import kotlinx.coroutines.launch

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductosViewModel by viewModels {
        ProductosViewModelFactory((requireActivity().application as FarmaciaApplication))
    }
    
    private lateinit var productosAdapter: ProductosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        
        // Cargar productos
        viewModel.loadProductos()
    }
    
    private fun setupRecyclerView() {
        productosAdapter = ProductosAdapter { producto ->
            viewModel.agregarAlCarrito(producto)
            Toast.makeText(requireContext(), "Agregado al carrito", Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerViewProductos.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productosAdapter
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productosState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = state.isLoading
                
                state.productos?.let { productos ->
                    productosAdapter.submitList(productos)
                    binding.emptyView.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
                }
                
                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProductos()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}