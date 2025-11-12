# 🎯 Guía Completa de Corrección - Android

## 📊 Resumen de Errores

**Total actual:** ~45 errores de compilación
**Reducción:** De 62 a 45 (27% resuelto)
**Archivos afectados:** 7 archivos Kotlin

## 🔴 ERRORES CRÍTICOS (Prioridad Alta)

### 1. BoletaViewModel Duplicado
**Archivo:** `BoletaViewModelMejorado.kt` línea 34-58  
**Error:** `Redeclaration: BoletaUiState`

**Problema:** Hay dos clases `BoletaUiState` - una en `BoletaViewModel.kt` y otra en `BoletaViewModelMejorado.kt`

**Solución:**
```kotlin
// En BoletaViewModelMejorado.kt, cambiar el nombre de la data class:
data class BoletaMejoradaUiState(
    val isLoading: Boolean = false,
    val boletaPdf: ByteArray? = null,  // Cambiar de 'boleta' a 'boletaPdf'
    val error: String? = null
)
```

Luego actualizar todas las referencias:
```kotlin
// Cambiar:
private val _uiState = MutableStateFlow(BoletaUiState())
// A:
private val _uiState = MutableStateFlow(BoletaMejoradaUiState())
```

### 2. Campos Faltantes en BoletaUiState
**Archivo:** `BoletaViewModelMejorado.kt`  
**Error:** `Cannot find a parameter with this name: transaccion` y `boletaPdf`

**Solución completa para `BoletaViewModelMejorado.kt`:**

```kotlin
package com.farmaciadey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.PagoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BoletaMejoradaUiState(
    val isLoading: Boolean = false,
    val boletaPdf: ByteArray? = null,
    val error: String? = null
)

class BoletaViewModelMejorado : ViewModel() {

    private val repository = PagoRepository()
    
    private val _uiState = MutableStateFlow(BoletaMejoradaUiState())
    val uiState: StateFlow<BoletaMejoradaUiState> = _uiState

    fun descargarBoletaTransaccion(transaccionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.descargarBoletaTransaccion(transaccionId)
            
            result.onSuccess { pdfBytes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    boletaPdf = pdfBytes
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Error al descargar boleta"
                )
            }
        }
    }

    fun descargarBoletaCompra(compraId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.descargarBoletaCompra(compraId)
            
            result.onSuccess { pdfBytes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    boletaPdf = pdfBytes
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Error al descargar boleta"
                )
            }
        }
    }

    fun limpiarBoleta() {
        _uiState.value = _uiState.value.copy(boletaPdf = null)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
```

### 3. Actualizar BoletaFragmentMejorado.kt

**Eliminar la sección de transaccion (líneas 127-132):**

```kotlin
// ELIMINAR ESTAS LÍNEAS:
// state.transaccion?.let { transaccion ->
//     binding.textViewMonto?.text = "Monto: S/ %.2f".format(transaccion.monto)
//     binding.textViewEstado?.text = "Estado: ${transaccion.estado}"
//     binding.textViewFecha?.text = "Fecha: ${transaccion.fechaCreacion ?: "N/A"}"
//     binding.textViewDescripcion?.text = transaccion.descripcion ?: "Sin descripción"
// }
```

**Eliminar el método cargarDatos (líneas 149-153):**

```kotlin
// ELIMINAR ESTE MÉTODO COMPLETO:
// private fun cargarDatos() {
//     transaccionId?.let { id ->
//         viewModel.cargarDatosTransaccion(id)
//     }
// }
```

## 🟡 ERRORES MEDIOS (Elementos XML Faltantes)

### 4. fragment_boleta.xml - Agregar elementos
**Ubicación:** Ya existe pero falta en el layout original de BoletaFragment.kt

Crear estos elementos adicionales:
```xml
<TextView
    android:id="@+id/tvTransaccionId"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="ID: "
    android:textStyle="bold"
    app:layout_constraintTop_toBottomOf="@id/textViewTitulo"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="16dp"
    android:layout_marginStart="16dp" />

<TextView
    android:id="@+id/tvMonto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Monto: S/ 0.00"
    android:textSize="18sp"
    android:textStyle="bold"
    app:layout_constraintTop_toBottomOf="@id/tvTransaccionId"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="8dp"
    android:layout_marginStart="16dp" />

<TextView
    android:id="@+id/tvMetodoPago"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Método: "
    app:layout_constraintTop_toBottomOf="@id/tvMonto"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="8dp"
    android:layout_marginStart="16dp" />

<TextView
    android:id="@+id/tvFecha"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Fecha: "
    app:layout_constraintTop_toBottomOf="@id/tvMetodoPago"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="8dp"
    android:layout_marginStart="16dp" />

<TextView
    android:id="@+id/tvEstado"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Estado: "
    app:layout_constraintTop_toBottomOf="@id/tvFecha"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="8dp"
    android:layout_marginStart="16dp" />

<Button
    android:id="@+id/btnDescargarBoleta"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="Descargar Boleta PDF"
    app:layout_constraintTop_toBottomOf="@id/tvEstado"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_margin="16dp" />

<Button
    android:id="@+id/btnVerHistorial"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="Ver Historial"
    app:layout_constraintTop_toBottomOf="@id/btnDescargarBoleta"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_marginHorizontal="16dp" />

<Button
    android:id="@+id/btnVolver"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="Volver"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_margin="16dp" />
```

### 5. fragment_transacciones.xml - Agregar elementos

```xml
<Button
    android:id="@+id/buttonActualizar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Actualizar"
    app:layout_constraintTop_toBottomOf="@id/textViewTitulo"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_margin="16dp" />

<Button
    android:id="@+id/buttonVolver"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="Volver"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_margin="16dp" />

<TextView
    android:id="@+id/textViewResumen"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="Resumen de transacciones"
    android:textStyle="bold"
    android:visibility="gone"
    app:layout_constraintTop_toBottomOf="@id/buttonActualizar"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_marginHorizontal="16dp" />
```

### 6. item_transaccion.xml - Agregar elementos

```xml
<!-- Dentro del ConstraintLayout existente, agregar: -->

<TextView
    android:id="@+id/textViewMetodoPago"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Método de pago"
    android:textSize="12sp"
    app:layout_constraintTop_toBottomOf="@id/textViewFecha"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="4dp" />

<TextView
    android:id="@+id/textViewReferencia"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Ref: ABC123"
    android:textSize="10sp"
    android:visibility="gone"
    app:layout_constraintTop_toBottomOf="@id/textViewMetodoPago"
    app:layout_constraintStart_toStartOf="parent"
    android:layout_marginTop="4dp" />
```

## 🟢 ERRORES MENORES (Ya documentados)

Ver `ELEMENTOS_FALTANTES_LAYOUTS.md` para:
- fragment_yape_plin.xml (3 elementos)
- fragment_visa.xml (4 elementos)

## 📝 PLAN DE ACCIÓN

### Orden de Corrección:

1. **Primero:** Arreglar BoletaViewModelMejorado.kt (CRÍTICO)
2. **Segundo:** Actualizar BoletaFragmentMejorado.kt  
3. **Tercero:** Agregar elementos XML en los layouts
4. **Cuarto:** Rebuild completo

### Comandos a Ejecutar:

```bash
# 1. Después de hacer los cambios en los archivos Kotlin
cd '/Users/pablohuerta/Documents/UTP/Ciclo_09/Integrador II/farmacia-android'

# 2. Limpiar
./gradlew clean

# 3. Rebuild
./gradlew assembleDebug

# 4. Si hay errores de cache
./gradlew clean build --no-build-cache
```

## ✅ Checklist de Verificación

- [ ] `BoletaViewModelMejorado.kt` corregido (cambiar BoletaUiState por BoletaMejoradaUiState)
- [ ] `BoletaFragmentMejorado.kt` sin referencias a `transaccion`
- [ ] `fragment_boleta.xml` con todos los elementos (tv*, btn*)
- [ ] `fragment_transacciones.xml` con buttonActualizar, buttonVolver, textViewResumen
- [ ] `item_transaccion.xml` con textViewMetodoPago, textViewReferencia
- [ ] `fragment_yape_plin.xml` con 3 elementos nuevos
- [ ] `fragment_visa.xml` con 4 elementos nuevos
- [ ] Rebuild exitoso sin errores

## 🎯 Resultado Esperado

Después de aplicar TODAS las correcciones:
- ✅ 0 errores de compilación
- ✅ APK genera correctamente
- ✅ Todos los Fragments funcionando

