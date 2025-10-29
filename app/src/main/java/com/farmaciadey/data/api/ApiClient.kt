package com.farmaciadey.data.api

import com.farmaciadey.BuildConfig
import com.farmaciadey.utils.PreferencesManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    private var retrofit: Retrofit? = null
    private var preferencesManager: PreferencesManager? = null
    
    fun init(preferencesManager: PreferencesManager) {
        this.preferencesManager = preferencesManager
    }
    
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()
            
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
    
    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor())
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    // Services
    val authService: AuthApiService by lazy { getRetrofit().create(AuthApiService::class.java) }
    val productoService: ProductoApiService by lazy { getRetrofit().create(ProductoApiService::class.java) }
    val usuarioService: UsuarioApiService by lazy { getRetrofit().create(UsuarioApiService::class.java) }
    val metodoPagoService: MetodoPagoApiService by lazy { getRetrofit().create(MetodoPagoApiService::class.java) }
    val compraService: CompraApiService by lazy { getRetrofit().create(CompraApiService::class.java) }
    
    // Interceptor para agregar token automáticamente
    private inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Verificar si ya tiene Authorization header
            if (originalRequest.header("Authorization") != null) {
                return chain.proceed(originalRequest)
            }
            
            // Solo agregar token si no es login
            if (originalRequest.url.encodedPath.contains("/auth/login")) {
                return chain.proceed(originalRequest)
            }
            
            // Agregar token si está disponible
            val token = preferencesManager?.getToken()
            return if (token != null) {
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
    }
}