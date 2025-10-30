package com.farmaciadey.utils

object NetworkUtils {
    
    /**
     * Convierte URLs de localhost para que funcionen en el emulador Android
     * También corrige los puertos incorrectos del backend
     * Ejemplo: localhost:7003 -> 10.0.2.2:7013
     */
    fun convertUrlForEmulator(url: String?): String? {
        if (url.isNullOrEmpty()) return url
        
        var convertedUrl = url
        
        // Primero convertir localhost a IP del emulador
        convertedUrl = convertedUrl.replace("localhost:", "10.0.2.2:")
        
        // Luego corregir los puertos incorrectos que viene del backend
        convertedUrl = convertedUrl.replace(":7001", ":7011") // auth
        convertedUrl = convertedUrl.replace(":7002", ":7012") // usuario  
        convertedUrl = convertedUrl.replace(":7003", ":7013") // producto
        convertedUrl = convertedUrl.replace(":7004", ":7014") // metodopago
        convertedUrl = convertedUrl.replace(":7005", ":7015") // compra
        
        return convertedUrl
    }
}
