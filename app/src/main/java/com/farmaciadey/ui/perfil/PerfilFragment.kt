package com.farmaciadey.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentPerfilBinding
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PerfilViewModel by viewModels {
        PerfilViewModelFactory(requireActivity().application as FarmaciaApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.perfilState.collect { state ->
                when {
                    state.isLoading -> {
                        // Mostrar loading si es necesario
                    }
                    state.error != null -> {
                        // Mostrar error
                        binding.tvNombre.text = "Error al cargar"
                        binding.tvEmail.text = state.error
                    }
                    state.usuario != null -> {
                        displayUserInfo(state.usuario)
                    }
                }
            }
        }
    }
    
    private fun displayUserInfo(usuario: com.farmaciadey.data.models.Usuario) {
        with(binding) {
            tvNombre.text = "${usuario.nombres} ${usuario.apellidos}"
            tvEmail.text = usuario.email ?: "Email no disponible"
            tvRol.text = usuario.rol
            tvUsuario.text = "Usuario: ${usuario.username}"
            tvTelefono.text = "Teléfono: ${usuario.telefono ?: "No disponible"}"
            tvDireccion.text = "Dirección: ${usuario.direccion ?: "No disponible"}"
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion()
            // MainActivity detectará automáticamente el cambio en tokenFlow y navegará al login
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}