# 🐛 Errores Comunes en BoletaFragmentMejorado y Soluciones

## 📋 Lista de Errores Identificados

### ❌ Error 1: ViewModel Incorrecto
**Problema:**
```kotlin
private val viewModel: BoletaViewModel by viewModels()
```
**Error:** `Unresolved reference: BoletaViewModel`

**Solución:**
```kotlin
private val viewModel: BoletaViewModelMejorado by viewModels()
```

**Importación necesaria:**
```kotlin
import com.farmaciadey.ui.viewmodel.BoletaViewModelMejorado
```

---

### ❌ Error 2: Referencias Null en ViewBinding
**Problema:**
```kotlin
binding.textViewTitulo?.text = "..."  // Usando ?. cuando no es nullable
```
**Error:** `Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver`

**Solución - Opción A (si existen en el layout):**
```kotlin
binding.textViewTitulo.text = "..."  // Sin ?
```

**Solución - Opción B (si son opcionales):**
```kotlin
// Mantener el ?. si realmente son opcionales en algunos layouts
binding.textViewTitulo?.text = "..."
```

---

### ❌ Error 3: Método No Existente
**Problema:**
```kotlin
state.transaccion?.let { transaccion ->
    binding.textViewMonto?.text = "Monto: S/ %.2f".format(transaccion.monto)
    // ...
}
```
**Error:** `Unresolved reference: transaccion` (si el UiState no tiene este campo)

**Solución:**
Comentar o eliminar si `BoletaUiState` no tiene campo `transaccion`:
```kotlin
// Eliminar esta sección si no existe el campo
// state.transaccion?.let { ... }
```

---

### ❌ Error 4: Intent sin Import Completo
**Problema:**
```kotlin
val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
```
**Error:** Código verboso y potenciales errores

**Solución:**
```kotlin
import android.content.Intent  // Al inicio del archivo

// Luego usar:
val intent = Intent(Intent.ACTION_VIEW)
```

---

### ❌ Error 5: DownloadManager Sin Usar
**Problema:**
```kotlin
val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) 
    as DownloadManager
// Variable declarada pero nunca usada
```

**Solución:**
Eliminar esta línea ya que no se utiliza:
```kotlin
// ELIMINAR:
// val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) 
//     as DownloadManager
```

---

### ❌ Error 6: FragmentBoletaBinding No Generado
**Problema:**
```kotlin
import com.farmaciadey.databinding.FragmentBoletaBinding
```
**Error:** `Unresolved reference: FragmentBoletaBinding`

**Causa:** El archivo `fragment_boleta.xml` no existe o ViewBinding no está habilitado.

**Solución:**

1. **Verificar ViewBinding en build.gradle:**
```gradle
android {
    buildFeatures {
        viewBinding true
    }
}
```

2. **Crear `fragment_boleta.xml` en `res/layout/`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/textViewTitulo"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="20sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="24dp" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <Button
        android:id="@+id/buttonDescargarTransaccion"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Descargar Boleta de Transacción"
        app:layout_constraintTop_toBottomOf="@id/textViewTitulo"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_margin="16dp" />

    <Button
        android:id="@+id/buttonDescargarCompra"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Descargar Boleta de Compra"
        app:layout_constraintTop_toBottomOf="@id/buttonDescargarTransaccion"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_margin="16dp" />

    <Button
        android:id="@+id/buttonVerTransacciones"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Ver Transacciones"
        app:layout_constraintTop_toBottomOf="@id/buttonDescargarCompra"
        app:layout_constraintStart_toStartOf="parent"
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

</androidx.constraintlayout.widget.ConstraintLayout>
```

3. **Rebuild el proyecto:**
```
Build → Clean Project
Build → Rebuild Project
```

---

### ❌ Error 7: FileProvider No Configurado
**Problema:**
```kotlin
FileProvider.getUriForFile(
    requireContext(),
    "${requireContext().packageName}.fileprovider",
    file
)
```
**Error en runtime:** `IllegalArgumentException: Failed to find configured root`

**Solución:**

1. **Crear `res/xml/file_paths.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="downloads" path="Download/" />
    <external-path name="external_downloads" path="Download/" />
</paths>
```

2. **Agregar en `AndroidManifest.xml`:**
```xml
<application>
    <!-- Otros elementos... -->
    
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>
```

---

### ❌ Error 8: Permisos Faltantes
**Problema:**
Runtime crash por falta de permisos de almacenamiento

**Solución:**

Agregar en `AndroidManifest.xml`:
```xml
<manifest>
    <!-- Para Android 9 y anteriores -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    
    <!-- Para Android 10+ (opcional, solo para acceso a archivos públicos) -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
</manifest>
```

---

### ❌ Error 9: TransaccionesFragment No Encontrado
**Problema:**
```kotlin
val transaccionesFragment = TransaccionesFragment().apply {
    arguments = bundle
}
```
**Error:** `Unresolved reference: TransaccionesFragment`

**Solución:**
```kotlin
import com.farmaciadey.ui.fragment.TransaccionesFragment
```

Si no existe el archivo, créalo primero.

---

### ❌ Error 10: R.id.fragmentContainer No Existe
**Problema:**
```kotlin
.replace(R.id.fragmentContainer, transaccionesFragment)
```
**Error:** `Unresolved reference: fragmentContainer`

**Solución:**
Usar el ID correcto de tu Activity principal. Opciones comunes:
```kotlin
// Opción 1:
.replace(R.id.fragment_container, transaccionesFragment)

// Opción 2:
.replace(R.id.nav_host_fragment, transaccionesFragment)

// Opción 3:
.replace(R.id.main_container, transaccionesFragment)
```

Verifica en tu `activity_main.xml` cuál es el ID del contenedor.

---

## 🔧 Pasos para Corregir TODOS los Errores

### Paso 1: Actualizar Imports
```kotlin
package com.farmaciadey.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.farmaciadey.R
import com.farmaciadey.databinding.FragmentBoletaBinding
import com.farmaciadey.ui.viewmodel.BoletaViewModelMejorado
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
```

### Paso 2: Corregir ViewModel
```kotlin
private val viewModel: BoletaViewModelMejorado by viewModels()
```

### Paso 3: Remover Safe Calls Innecesarios
Si los elementos existen en el layout, cambiar:
```kotlin
// DE:
binding.textViewTitulo?.text = "..."

// A:
binding.textViewTitulo.text = "..."
```

### Paso 4: Eliminar Código No Usado
```kotlin
// ELIMINAR sección de transaccion si no existe en UiState
// state.transaccion?.let { ... }

// ELIMINAR DownloadManager sin uso
// val downloadManager = ...
```

### Paso 5: Crear Layout XML
Ver "Error 6" arriba para el código completo.

### Paso 6: Configurar FileProvider
Ver "Error 7" arriba para la configuración completa.

### Paso 7: Agregar Permisos
Ver "Error 8" arriba para los permisos en Manifest.

### Paso 8: Rebuild Proyecto
```
1. Build → Clean Project
2. Build → Rebuild Project
3. File → Invalidate Caches → Invalidate and Restart
```

---

## ✅ Checklist de Verificación

- [ ] Todos los imports están correctos
- [ ] `BoletaViewModelMejorado` está importado
- [ ] `fragment_boleta.xml` existe en `res/layout/`
- [ ] ViewBinding está habilitado en `build.gradle`
- [ ] FileProvider configurado en `AndroidManifest.xml`
- [ ] `file_paths.xml` creado en `res/xml/`
- [ ] Permisos agregados en `AndroidManifest.xml`
- [ ] Sin referencias a métodos que no existen
- [ ] Sin variables declaradas sin usar
- [ ] Proyecto rebuildeado completamente

---

## 🎯 Código Final Corregido

El archivo `BoletaFragmentMejorado_CORREGIDO.kt` contiene todas las correcciones aplicadas.

### Principales Cambios:
1. ✅ ViewModel correcto: `BoletaViewModelMejorado`
2. ✅ Intent simplificado con import
3. ✅ Manejo de storage para Android 10+
4. ✅ Eliminado código sin usar
5. ✅ Safe calls apropiados
6. ✅ Comentado código de transaccion si no existe

---

## 📞 Necesitas Ayuda?

Si sigues teniendo errores, comparte:
1. El mensaje de error exacto de Android Studio
2. La línea de código que causa el error
3. El contenido de `BoletaUiState` (para verificar campos)

