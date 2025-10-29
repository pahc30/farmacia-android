package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val id: Long? = null,
    val codigo: String,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val url: String? = null,
    val categoria: Categoria? = null
) : Parcelable

@Parcelize
data class Categoria(
    val id: Long? = null,
    val nombre: String,
    val descripcion: String? = null
) : Parcelable