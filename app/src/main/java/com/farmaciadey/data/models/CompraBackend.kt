package com.farmaciadey.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CompraBackend(
    val id: Int?,
    val codigo: String?,
    val fecha: Date?,
    val usuarioId: Int?,
    val metodoPagoId: Int?,
    val igv: Double = 0.18,
    val subtotal: Double?,
    val total: Double?,
    val eliminado: Int = 0,
    val detalleCompra: List<DetalleCompraBackend>?,
    val metodoPago: String?
)

data class DetalleCompraBackend(
    val id: Int?,
    val cantidad: Int?,
    val precioUnitario: Double?,
    val subtotal: Double?,
    val productoId: Int?,
    val producto: ProductoBackend?
)

data class ProductoBackend(
    val id: Int?,
    val codigo: String?,
    val nombre: String?,
    val precio: Double?,
    val stock: Int?,
    val url: String?,
    val categoria: String?
)