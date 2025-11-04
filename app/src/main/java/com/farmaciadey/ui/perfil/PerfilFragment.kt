package com.farmaciadey.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        setupListeners()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun updateUI(state: PerfilState) {
        // Mostrar datos del usuario
        state.usuario?.let { usuario ->
            binding.tvNombre.text = "${usuario.nombres ?: ""} ${usuario.apellidos ?: ""}".trim()
            binding.tvEmail.text = usuario.email ?: "No especificado"
            binding.tvUsuario.text = "Usuario: ${usuario.nombres ?: "No especificado"}"
            binding.tvTelefono.text = "Teléfono: ${if (usuario.telefono?.isNotBlank() == true) usuario.telefono else "No especificado"}"
            binding.tvDireccion.text = "Dirección: ${if (usuario.direccion?.isNotBlank() == true) usuario.direccion else "No especificada"}"
            binding.tvRol.text = usuario.rol ?: "Usuario"
        }
        
        // Mostrar error si existe
        if (state.error != null) {
            Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupListeners() {
        // Navegar al historial de compras usando acción global
        binding.btnHistorialCompras.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_global_historial_compras)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al navegar al historial", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion()
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }
        
        // Editar perfil completo
        binding.btnEditarPerfil.setOnClickListener {
            abrirDialogEditarPerfil()
        }
        
        // Botones de edición individual
        binding.btnEditarTelefono.setOnClickListener {
            abrirDialogEditarPerfil()
        }
        
        binding.btnEditarDireccion.setOnClickListener {
            abrirDialogEditarPerfil()
        }
    }
    
    private fun abrirDialogEditarPerfil() {
        val usuario = viewModel.uiState.value.usuario
        if (usuario != null) {
            val dialog = EditarPerfilDialogFragment.newInstance(usuario)
            dialog.show(childFragmentManager, "editar_perfil")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
