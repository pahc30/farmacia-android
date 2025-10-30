# 📱 Farmacia DEY - Android App

> Aplicación móvil Android para la farmacia desarrollada con Kotlin y arquitectura MVVM

## 🎯 Descripción

Aplicación móvil que permite a los usuarios navegar productos farmacéuticos, agregarlos a un carrito de compras y realizar autenticación. Se conecta a un backend de microservicios Spring Boot.

## ✨ Características

- 🔐 **Autenticación JWT** con credenciales test1/test1
- 📦 **Catálogo de productos** con 3 productos disponibles
- 🛒 **Carrito de compras** con almacenamiento temporal durante la sesión
- 🎨 **Material Design** con navegación inferior
- 🔍 **Búsqueda en tiempo real** de productos
- 🖼️ **Carga de imágenes** con integración Glide
- 🌐 **Conectividad backend** con 5 microservicios

## 🏗️ Arquitectura

- **Patrón MVVM** con StateFlow y Coroutines
- **Repository Pattern** para gestión de datos
- **Clean Architecture** con separación de responsabilidades
- **Componentes lifecycle-aware**

## 🛠️ Stack Técnico

- **Lenguaje**: Kotlin 100%
- **Android API**: 24-34 (Android 7.0 - 14)
- **Networking**: Retrofit + OkHttp
- **UI**: View Binding + Material Design
- **Navegación**: Navigation Component
- **Imágenes**: Glide

## 📱 Funcionalidades Implementadas

### ✅ Completamente funcional:
1. **Login** → Credenciales test1/test1
2. **Productos** → Carga exitosa de 3 productos
3. **Carrito** → Agregar/quitar productos
4. **Navegación** → Entre fragmentos sin pérdida de estado
5. **Cálculos** → Total automático del carrito

### 🧪 Flujo probado:
- Paracetamol ($10) + Ibuprofeno ($100) = **$110 total** ✅

## 🚀 Configuración

### Prerrequisitos
- Android Studio Arctic Fox o superior
- JDK 8 o superior
- Android SDK API 24+

### Backend
La aplicación se conecta a microservicios en:
- **Gateway**: Puerto 9000
- **Auth**: Puerto 7011
- **Productos**: Puerto 7012
- **Usuario**: Puerto 7013
- **Compra**: Puerto 7014
- **Método Pago**: Puerto 7015

### Instalación
1. Clonar el repositorio
2. Abrir en Android Studio
3. Sincronizar dependencias Gradle
4. Ejecutar en emulador o dispositivo

## 📋 Documentación

Consultar `RESUMEN_PROYECTO.md` para documentación técnica completa incluyendo:
- Ejemplos de código
- Explicación de arquitectura
- Detalles de implementación
- Logros técnicos

## 🎯 Estado del Proyecto

**✅ COMPLETAMENTE FUNCIONAL**
- Todas las funcionalidades implementadas
- Integración backend exitosa
- UI/UX responsiva
- Código limpio y mantenible

---

**Desarrollado**: Octubre 2025  
**Versión**: 1.0.0  
**Estado**: Producción ready 🚀

Aplicación móvil Android para el sistema de farmacia Farmacia DeY.

## 📱 Características

- **Autenticación**: Login con JWT
- **Catálogo**: Visualización de productos disponibles
- **Carrito**: Gestión de productos para compra
- **Perfil**: Información del usuario y historial

## 🏗️ Arquitectura

- **MVVM**: Model-View-ViewModel pattern
- **Retrofit**: Cliente HTTP para APIs REST
- **Navigation Component**: Navegación entre pantallas
- **DataStore**: Almacenamiento de preferencias
- **Coroutines**: Programación asíncrona

## 🔧 Configuración

### Requisitos

- Android Studio Arctic Fox o superior
- SDK mínimo: API 24 (Android 7.0)
- SDK objetivo: API 34 (Android 14)
- Kotlin 1.9.20

### URLs de Backend

La aplicación consume las siguientes APIs:

```
Base URL (Emulador): http://10.0.2.2:9000/
Base URL (Dispositivo): http://192.168.1.X:9000/

Endpoints:
- POST /auth/login - Autenticación
- GET /producto/list - Lista de productos
- GET /usuario/perfil - Perfil de usuario
- POST /compra/crear - Crear compra
- GET /metodopago/list - Métodos de pago
```

### Instalación

1. Clonar el repositorio
2. Abrir en Android Studio
3. Sincronizar proyecto con Gradle
4. Asegurar que el backend esté ejecutándose
5. Ejecutar la aplicación

## 📦 Estructura del Proyecto

```
app/src/main/java/com/farmaciadey/
├── data/
│   ├── api/          # Servicios Retrofit
│   ├── models/       # Modelos de datos
│   └── repository/   # Repositorios
├── ui/
│   ├── auth/         # Pantallas de autenticación
│   ├── productos/    # Catálogo de productos
│   ├── carrito/      # Carrito de compras
│   └── perfil/       # Perfil de usuario
└── utils/            # Utilidades y helpers
```

## 🧪 Testing

- Usuario de prueba: `test1`
- Contraseña: `test1`

## 🚀 Despliegue

Para desplegar en dispositivo físico:

1. Cambiar `BASE_URL` en `build.gradle` a la IP del servidor
2. Generar APK firmado
3. Instalar en dispositivo

---

Desarrollado para el curso Integrador II - UTP