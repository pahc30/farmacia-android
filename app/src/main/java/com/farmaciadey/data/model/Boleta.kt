package com.farmaciadey.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoletaRequest(
    val transaccionId: Long? = null,
    val compraId: Long? = null
) : Parcelable

@Parcelize
data class BoletaResponse(
    val success: Boolean,
    val message: String,
    val downloadUrl: String? = null,
    val fileName: String? = null,
    val pdfContent: ByteArray? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoletaResponse

        if (success != other.success) return false
        if (message != other.message) return false
        if (downloadUrl != other.downloadUrl) return false
        if (fileName != other.fileName) return false
        if (pdfContent != null) {
            if (other.pdfContent == null) return false
            if (!pdfContent.contentEquals(other.pdfContent)) return false
        } else if (other.pdfContent != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + (downloadUrl?.hashCode() ?: 0)
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + (pdfContent?.contentHashCode() ?: 0)
        return result
    }
}