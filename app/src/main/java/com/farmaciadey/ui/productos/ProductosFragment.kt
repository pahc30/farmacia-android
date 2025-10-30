package com.farmaciadey.ui.productos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.databinding.FragmentProductosBinding
import kotlinx.coroutines.launch

class ProductosFragment : Fragment() {
    
    companion object {
        private const val TAG = "ProductosFragment"
    }
    
    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductosViewModel by viewModels {
        val app = requireActivity().application as FarmaciaApplication
        ProductosViewModelFactory(app)
    }
    
    private lateinit var adapter: ProductosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        Log.d(TAG, "ProductosFragment onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "ProductosFragment onViewCreated")
        
        setupRecyclerView()
        setupObservers()
        setupSearchView()
        setupSwipeRefresh()
        
        // Cargar productos inmediatamente
        viewModel.loadProductos()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductosAdapter { producto, cantidad ->
            viewModel.agregarAlCarrito(producto, cantidad)
        }
        
        binding.recyclerViewProductos.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@ProductosFragment.adapter
        }
    }
    
    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.searchProductos(query)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProductos()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productosState.collect { state ->
                Log.d(TAG, "UI State cambió: loading=${state.isLoading}, productos=${state.productos?.size}, error=${state.error}")
                
                binding.progressBar.visibility = if (state.isLoading && !binding.swipeRefresh.isRefreshing) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                state.productos?.let { productos ->
                    if (productos.isNotEmpty()) {
                        binding.recyclerViewProductos.visibility = View.VISIBLE
                        binding.emptyView.visibility = View.GONE
                        adapter.submitList(productos)
                    } else {
                        binding.recyclerViewProductos.visibility = View.GONE
                        binding.emptyView.visibility = View.VISIBLE
                        binding.emptyView.text = if (state.searchQuery.isNotEmpty()) {
                            "No se encontraron productos con '${state.searchQuery}'"
                        } else {
                            "No hay productos disponibles"
                        }
                    }
                }
                
                state.error?.let { error ->
                    binding.recyclerViewProductos.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                    binding.emptyView.text = error
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
