package com.farmaciadey.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

object NetworkUtils {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    fun showNetworkError(context: Context) {
        Toast.makeText(context, "Sin conexión a internet", Toast.LENGTH_LONG).show()
    }
}

object Constants {
    const val EMULATOR_LOCALHOST = "10.0.2.2"
    const val REAL_LOCALHOST = "localhost"
    const val API_PORT = "9000"
    
    // URLs
    const val BASE_URL_EMULATOR = "http://$EMULATOR_LOCALHOST:$API_PORT/"
    const val BASE_URL_REAL = "http://$REAL_LOCALHOST:$API_PORT/"
    
    // Request timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // JWT
    const val TOKEN_PREFIX = "Bearer "
}