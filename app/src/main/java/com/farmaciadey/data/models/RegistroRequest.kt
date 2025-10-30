package com.farmaciadey.data.models

data class RegistroRequest(
    val nombres: String,
    val apellidos: String,
    val identificacion: String,
    val email: String,
    val telefono: String,
    val username: String,  // Cambiado de usuario a username
    val password: String,
    val rol: String = "USER",  // Agregar campo rol con valor por defecto
    val estado: Int = 1
)