package com.farmaciadey.data.models

data class RegistroResponse(
    val estado: Int,
    val mensaje: String?,
    val dato: Usuario?
)