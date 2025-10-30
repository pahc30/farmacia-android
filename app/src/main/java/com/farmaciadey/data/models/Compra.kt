package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Compra(
    val id: Long? = null,
    val usuarioId: Long,
    val fecha: String? = null,
    val total: Double,
    val metodoPagoId: Long,
    val detalles: List<DetalleCompra> = emptyList()
) : Parcelable

@Parcelize
data class DetalleCompra(
    val id: Long? = null,
    val productoId: Long,
    val cantidad: Int,
    val precio: Double,
    val producto: Producto? = null
) : Parcelable

@Parcelize
data class ItemCarrito(
    val id: Int? = null, // ID del item en el carrito para poder eliminarlo del backend
    val producto: Producto,
    var cantidad: Int = 1
) : Parcelable {
    val subtotal: Double
        get() = producto.precio * cantidad
}