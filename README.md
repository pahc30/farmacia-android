# Farmacia DeY - Android App

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