package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetodoPago(
    val id: Long? = null,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
) : Parcelable