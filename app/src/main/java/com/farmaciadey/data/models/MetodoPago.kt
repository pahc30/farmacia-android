package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetodoPago(
    val id: Long? = null,
    val descripcion: String? = null,
    val eliminado: Int = 0,
    val tipo: String // "Visa", "Yape/Plin", "Efectivo"
) : Parcelable {
    
    // Computed properties para UI
    val isActive: Boolean get() = eliminado == 0
    val displayName: String get() = tipo
    val icon: Int get() = when(tipo) {
        "Visa" -> android.R.drawable.ic_secure
        "Yape/Plin" -> android.R.drawable.ic_dialog_email
        else -> android.R.drawable.ic_menu_help
    }
}
