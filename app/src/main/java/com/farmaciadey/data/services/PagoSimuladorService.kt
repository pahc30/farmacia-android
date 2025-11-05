package com.farmaciadey.data.services

import com.farmaciadey.data.models.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Servicio para simular pagos de forma local sin servicios externos
 * Simula comportamientos realistas de Yape/Plin y Visa
 */
class PagoSimuladorService {
    
    suspend fun simularPago(simulacion: SimulacionPago): PagoResponse {
        return when (simulacion.metodoPago.tipo) {
            "Visa" -> simularPagoVisa(simulacion)
            "Yape/Plin" -> simularPagoYapePlin(simulacion)
            else -> simularPagoGenerico(simulacion)
        }
    }
    
    private suspend fun simularPagoVisa(simulacion: SimulacionPago): PagoResponse {
        // Simular tiempo de procesamiento de tarjeta (2-5 segundos)
        val tiempoProcesamiento = Random.nextLong(2000, 5000)
        delay(tiempoProcesamiento)
        
        // Visa tiene alta probabilidad de éxito pero puede fallar por fondos insuficientes
        val exito = Random.nextFloat() < 0.95f
        
        return if (exito) {
            PagoResponse(
                success = true,
                transaccionId = generateTransactionId(),
                estado = "COMPLETADA",
                monto = simulacion.monto,
                message = "Pago con tarjeta Visa procesado exitosamente",
                referenciaExterna = "VISA_${System.currentTimeMillis()}"
            )
        } else {
            PagoResponse(
                success = false,
                estado = "FALLIDA",
                monto = simulacion.monto,
                message = "Pago rechazado: Fondos insuficientes en la tarjeta"
            )
        }
    }
    
    private suspend fun simularPagoYapePlin(simulacion: SimulacionPago): PagoResponse {
        // Simular tiempo de procesamiento de billetera digital (1-3 segundos)
        val tiempoProcesamiento = Random.nextLong(1000, 3000)
        delay(tiempoProcesamiento)
        
        // Yape/Plin generalmente es más rápido y confiable
        val exito = Random.nextFloat() < 0.98f
        
        return if (exito) {
            PagoResponse(
                success = true,
                transaccionId = generateTransactionId(),
                estado = "COMPLETADA",
                monto = simulacion.monto,
                message = "Pago con ${simulacion.metodoPago.tipo} completado exitosamente",
                referenciaExterna = "YAPE_${System.currentTimeMillis()}"
            )
        } else {
            PagoResponse(
                success = false,
                estado = "FALLIDA",
                monto = simulacion.monto,
                message = "Error de conexión con ${simulacion.metodoPago.tipo}. Intente nuevamente."
            )
        }
    }
    
    private suspend fun simularPagoGenerico(simulacion: SimulacionPago): PagoResponse {
        delay(simulacion.duracionSimulacion)
        
        val exito = Random.nextFloat() < simulacion.probabilidadExito
        
        return if (exito) {
            PagoResponse(
                success = true,
                transaccionId = generateTransactionId(),
                estado = "COMPLETADA",
                monto = simulacion.monto,
                message = "Pago procesado exitosamente"
            )
        } else {
            PagoResponse(
                success = false,
                estado = "FALLIDA",
                monto = simulacion.monto,
                message = "Error al procesar el pago"
            )
        }
    }
    
    /**
     * Simula estados intermedios del pago para UX realista
     */
    suspend fun simularEstadosIntermedio(callback: (String) -> Unit) {
        callback("Iniciando transacción...")
        delay(500)
        
        callback("Validando datos...")
        delay(1000)
        
        callback("Procesando pago...")
        delay(1500)
        
        callback("Confirmando transacción...")
        delay(800)
    }
    
    private fun generateTransactionId(): Long {
        return System.currentTimeMillis() + Random.nextLong(1000, 9999)
    }
    
    /**
     * Genera mensajes específicos según el método de pago
     */
    fun getMensajeMetodoPago(tipo: String): String {
        return when(tipo) {
            "Visa" -> "Será redirigido a la pasarela segura de su banco"
            "Yape/Plin" -> "Abra su app de $tipo para completar el pago"
            "Efectivo" -> "Conserve este código para pagar en tienda"
            else -> "Procesando pago..."
        }
    }
}
