package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val id: Long? = null,
    val username: String,
    val email: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val activo: Boolean = true,
    val rol: String? = "CLIENTE"
) : Parcelable