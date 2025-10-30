package com.farmaciadey.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.JsonAdapter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonToken

@Parcelize
data class Producto(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val url: String,
    @JsonAdapter(CategoriaStringAdapter::class)
    val categoria: String,
    val eliminado: Int = 0
) : Parcelable

class CategoriaStringAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        out.value(value)
    }

    override fun read(reader: JsonReader): String {
        return when (reader.peek()) {
            JsonToken.STRING -> reader.nextString()
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()
                var nombre = ""
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "nombre" -> nombre = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                nombre
            }
            else -> {
                reader.skipValue()
                ""
            }
        }
    }
}