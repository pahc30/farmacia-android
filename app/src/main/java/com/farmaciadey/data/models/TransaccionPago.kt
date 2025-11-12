package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransaccionPago(
    val id: Long? = null,
    val compraId: Long,
    val metodoPagoId: Long,
    val monto: Double,
    val moneda: String = "PEN",
    val estado: EstadoPago,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null,
    val fechaPago: String? = null,
    val referenciaExterna: String? = null,
    val clientSecret: String? = null,  // Agregado para soportar flujo completo de pagos
    val descripcion: String? = null,
    val detallesRespuesta: String? = null,
    val mensajeError: String? = null,
    val eliminado: Int = 0
) : Parcelable

enum class EstadoPago {
    PENDIENTE,
    PROCESANDO,
    COMPLETADA,
    FALLIDA,
    CANCELADA,
    REEMBOLSADA
}

// Request models para API
@Parcelize
data class CrearPagoRequest(
    val compraId: Long,
    val metodoPagoId: Long,
    val monto: Double,
    val moneda: String = "PEN",
    val descripcion: String,
    val codigoPago: String? = null,
    val datosTarjeta: DatosTarjeta? = null
) : Parcelable

@Parcelize
data class DatosTarjeta(
    val numeroTarjeta: String,
    val fechaExpiracion: String,
    val cvv: String,
    val nombreTitular: String
) : Parcelable

// Modelos para la nueva API de pagos
@Parcelize
data class PaymentIntentRequest(
    val compraId: Long,
    val monto: Double,
    val moneda: String = "PEN",
    val descripcion: String,
    val emailComprador: String? = null,
    val nombreComprador: String? = null
) : Parcelable

@Parcelize
data class PaymentIntentResponse(
    val success: Boolean,
    val transaccionId: Long? = null,
    val clientSecret: String? = null,
    val stripePaymentIntentId: String? = null,
    val message: String? = null,
    val error: String? = null
) : Parcelable

@Parcelize
data class PagoResponse(
    val success: Boolean,
    val transaccionId: Long? = null,
    val estado: String? = null,
    val monto: Double? = null,
    val message: String,
    val referenciaExterna: String? = null
) : Parcelable

// Para la simulación de pagos
@Parcelize
data class SimulacionPago(
    val metodoPago: MetodoPago,
    val monto: Double,
    val duracionSimulacion: Long = 3000L, // 3 segundos por defecto
    val probabilidadExito: Float = 0.9f // 90% de éxito por defecto
) : Parcelable
