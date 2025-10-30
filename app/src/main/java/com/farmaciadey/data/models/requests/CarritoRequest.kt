package com.farmaciadey.data.models.requests

data class CarritoRequest(
    val usuarioId: Int,
    val productoId: Int,
    val cantidad: Int
)