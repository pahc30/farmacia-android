package com.farmaciadey.ui.perfil

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.Usuario
import com.farmaciadey.databinding.DialogEditarPerfilBinding
import kotlinx.coroutines.launch

class EditarPerfilDialogFragment : DialogFragment() {
    
    private var _binding: DialogEditarPerfilBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PerfilViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        PerfilViewModelFactory(requireActivity().application as FarmaciaApplication)
    }
    
    private var usuario: Usuario? = null
    
    companion object {
        fun newInstance(usuario: Usuario): EditarPerfilDialogFragment {
            val fragment = EditarPerfilDialogFragment()
            val args = Bundle()
            // No necesitamos pasar el usuario como argument ya que compartimos el ViewModel
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setWindowAnimations(android.R.style.Animation_Dialog)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditarPerfilBinding.inflate(inflater, container, false)
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
                // Llenar campos con datos del usuario
                state.usuario?.let { user ->
                    if (usuario == null) { // Solo llenar la primera vez
                        usuario = user
                        binding.etNombres.setText(user.nombres)
                        binding.etApellidos.setText(user.apellidos)
                        binding.etEmail.setText(user.email)
                        binding.etTelefono.setText(user.telefono)
                        binding.etDireccion.setText(user.direccion)
                    }
                }
                
                // Mostrar/ocultar loading
                binding.progressBar.visibility = if (state.isUpdating) View.VISIBLE else View.GONE
                binding.btnGuardar.isEnabled = !state.isUpdating
                
                // Mostrar éxito
                if (state.updateSuccess) {
                    Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                
                // Mostrar error
                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnCancelar.setOnClickListener {
            dismiss()
        }
        
        binding.btnGuardar.setOnClickListener {
            guardarCambios()
        }
    }
    
    private fun guardarCambios() {
        val usuarioActualizado = usuario?.copy(
            nombres = binding.etNombres.text.toString().trim(),
            apellidos = binding.etApellidos.text.toString().trim(),
            telefono = binding.etTelefono.text.toString().trim(),
            direccion = binding.etDireccion.text.toString().trim()
        )
        
        if (usuarioActualizado != null) {
            if (usuarioActualizado.nombres.isBlank()) {
                binding.tilNombres.error = "El nombre es requerido"
                return
            }
            if (usuarioActualizado.apellidos.isBlank()) {
                binding.tilApellidos.error = "Los apellidos son requeridos"
                return
            }
            
            // Limpiar errores
            binding.tilNombres.error = null
            binding.tilApellidos.error = null
            
            viewModel.actualizarPerfil(
                usuarioActualizado.nombres,
                usuarioActualizado.apellidos,
                usuarioActualizado.email ?: "",
                usuarioActualizado.telefono ?: "",
                usuarioActualizado.direccion ?: ""
            )
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}