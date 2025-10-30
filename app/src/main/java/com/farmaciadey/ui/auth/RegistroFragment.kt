package com.farmaciadey.ui.auth

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
import com.farmaciadey.databinding.FragmentRegistroBinding
import kotlinx.coroutines.launch

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RegistroViewModel by viewModels {
        RegistroViewModelFactory((requireActivity().application as FarmaciaApplication).preferencesManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Botón registrarse
        binding.btnRegistrarse.setOnClickListener {
            registrarUsuario()
        }
        
        // Enlace para volver al login
        binding.tvVolverLogin.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // Limpiar errores al escribir
        val editTexts = listOf(
            binding.etNombres,
            binding.etApellidos,
            binding.etIdentificacion,
            binding.etEmail,
            binding.etTelefono,
            binding.etUsuario,
            binding.etPassword,
            binding.etConfirmPassword
        )
        
        editTexts.forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    viewModel.clearError()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Loading
                binding.btnRegistrarse.isEnabled = !state.isLoading
                binding.btnRegistrarse.text = if (state.isLoading) "Registrando..." else "Registrarse"
                
                // Error
                if (state.errorMessage != null) {
                    binding.tvError.text = state.errorMessage
                    binding.tvError.visibility = View.VISIBLE
                } else {
                    binding.tvError.visibility = View.GONE
                }
                
                // Éxito
                if (state.isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        state.successMessage ?: "Usuario registrado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Navegar de vuelta al login
                    findNavController().popBackStack()
                    viewModel.clearSuccess()
                }
            }
        }
    }

    private fun registrarUsuario() {
        val nombres = binding.etNombres.text.toString()
        val apellidos = binding.etApellidos.text.toString()
        val identificacion = binding.etIdentificacion.text.toString()
        val email = binding.etEmail.text.toString()
        val telefono = binding.etTelefono.text.toString()
        val usuario = binding.etUsuario.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        viewModel.registrarUsuario(
            nombres = nombres,
            apellidos = apellidos,
            identificacion = identificacion,
            email = email,
            telefono = telefono,
            usuario = usuario,
            password = password,
            confirmPassword = confirmPassword
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}