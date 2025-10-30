package com.farmaciadey.data.models.responses

import com.farmaciadey.data.models.Producto

data class CarritoResponse(
    val id: Int,
    val cantidad: Int,
    val producto: Producto
)