package com.farmaciadey.data.models

data class DataResponse<T>(
    val dato: T?,
    val estado: Int,
    val mensaje: String? = null
)