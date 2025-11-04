package com.farmaciadey.data.models

/**
 * Data Transfer Object para actualizar información del usuario
 * No incluye el campo password para evitar sobrescribirlo accidentalmente
 */
data class UpdateUsuarioRequest(
    val id: Int,
    val identificacion: String,
    val nombres: String,
    val apellidos: String,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val rol: String,
    val username: String
)
