package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val id: Int,
    val identificacion: String,
    val nombres: String,
    val apellidos: String,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val rol: String,
    val username: String,
    val eliminado: Int = 0
) : Parcelable