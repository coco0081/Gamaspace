# 🎮 Gamaspace - App Optimizadora de Juegos

Gamaspace es una aplicación Android moderna que optimiza el rendimiento de tu dispositivo y ofrece una experiencia de gaming mejorada. Diseñada con Kotlin, Jetpack Compose y Shizuku para máximo control del sistema.

## ✨ Características Principales

### 🔍 Monitor de Rendimiento Real-Time
- **RAM**: Monitoreo en tiempo real del uso de memoria RAM
- **CPU**: Seguimiento del uso del procesador
- **GPU**: Información de utilización de GPU
- **Batería**: Nivel de batería, temperatura y estado térmico
- **Estadísticas**: Máximos y promedios de la última hora

### 🎮 Lanzador de Juegos
- Detección automática de juegos instalados
- Búsqueda y filtrado rápido
- Lanzamiento optimizado con un clic
- Historial de sesiones de juego
- Estadísticas de rendimiento por juego

### ♻️ Limpieza de Memoria
- Escaneo de caché de aplicaciones
- Limpieza selectiva de caché por app
- Limpieza de archivos temporales
- Visualización del espacio liberado
- Gestión automática de datos antiguos

### ⚡ Perfiles de Optimización
- **Perfil Default**: Equilibrio entre rendimiento y consumo
- **Perfil Gaming**: Máximo rendimiento para juegos
- **Perfil Battery Saving**: Ahorro máximo de batería
- Personalización de perfiles (RAM, CPU, GPU, Brightness)
- Cambio rápido entre perfiles

## 🏗️ Arquitectura

La aplicación sigue la arquitectura **MVVM** (Model-View-ViewModel) con principios de clean architecture:

```
Gamaspace/
├── app/src/main/java/com/gamaspace/app/
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── MainScreen.kt
│   │   ├── screens/
│   │   │   ├── PerformanceMonitorScreen.kt
│   │   │   ├── GameLauncherScreen.kt
│   │   │   ├── MemoryCleanupScreen.kt
│   │   │   └── OptimizationProfileScreen.kt
│   │   ├── viewmodel/
│   │   │   ├── PerformanceMonitorViewModel.kt
│   │   │   ├── GameLauncherViewModel.kt
│   │   │   ├── MemoryCleanupViewModel.kt
│   │   │   └── OptimizationProfileViewModel.kt
│   │   └── theme/
│   │       └── Theme.kt
│   ├── data/
│   │   ├── database/
│   │   │   ├── GamaspaceDatabase.kt
│   │   │   └── Daos.kt
│   │   └── model/
│   │       └── Models.kt
│   ├── service/
│   │   ├── PerformanceMonitorService.kt
│   │   └── ShizukuService.kt
│   ├── repository/
│   │   └── GamaspaceRepository.kt
│   └── di/
│       └── DatabaseModule.kt
├── build.gradle.kts
└── AndroidManifest.xml
```

## 🛠️ Tecnologías Utilizadas

### Core Android
- **Kotlin** 1.9+
- **Jetpack Compose** - UI moderna declarativa
- **Material Design 3** - Diseño visual
- **Room Database** - Persistencia de datos
- **LiveData & Flow** - Reactividad

### Inyección de Dependencias
- **Hilt** - Inyección de dependencias simplificada

### Servicios Avanzados
- **Shizuku** - Acceso a operaciones del sistema sin root
- **WorkManager** - Tareas en segundo plano (opcional)

### Monitoreo
- Lectura de `/proc/stat` para CPU
- Lectura de estado de batería
- Lectura de memoria del dispositivo

## 📦 Instalación

### Requisitos
- Android 8 (API 26) - Android 16
- Mínimo 100 MB de almacenamiento
- Shizuku instalado y configurado (para funciones avanzadas)

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/coco0081/Gamaspace.git
cd Gamaspace
```

2. **Abrir en Android Studio**
```bash
# Asegúrate de tener Android Studio instalado
# File → Open → Selecciona la carpeta Gamaspace
```

3. **Compilar el proyecto**
```bash
./gradlew build
```

4. **Ejecutar la app**
```bash
./gradlew installDebug
# O presiona Run en Android Studio
```

## 🚀 Uso

### Monitor de Rendimiento
1. Abre la app y ve a la pestaña "Monitor"
2. Visualiza datos de RAM, CPU, GPU y Batería en tiempo real
3. Toca el ícono de actualizar para refrescar manualmente

### Lanzador de Juegos
1. Ve a la pestaña "Juegos"
2. Busca tu juego en la barra de búsqueda
3. Presiona el botón Play para lanzar con optimización automática

### Limpieza de Memoria
1. Ve a la pestaña "Limpieza"
2. Visualiza el caché total disponible
3. Presiona "Limpiar Todo" o selecciona apps individuales

### Perfiles de Optimización
1. Ve a la pestaña "Perfiles"
2. Selecciona un perfil para activarlo
3. Los ajustes se aplican inmediatamente

## ⚙️ Configuración de Shizuku

Para obtener todas las funcionalidades avanzadas:

### Linux/Mac
```bash
adb shell sh /data/adb/shizuku/starter.sh
```

### Windows
```cmd
adb shell sh /data/adb/shizuku/starter.sh
```

Luego autoriza Gamaspace en la app de Shizuku.

## 📊 Estructura de Datos

### Base de Datos Room
- **apps** - Registro de aplicaciones/juegos
- **performance_stats** - Historial de métricas
- **optimization_profiles** - Perfiles guardados
- **game_history** - Sesiones de juego
- **cache_data** - Información de caché
- **notifications** - Notificaciones del sistema

## 🎨 Temas

La app soporta:
- ✅ Tema oscuro (por defecto)
- ⚪ Tema claro (próximamente)
- 🌍 Material Design 3 Colors

## 🔐 Permisos Requeridos

```xml
<!-- Monitoreo de rendimiento -->
android.permission.GET_TASKS
android.permission.PACKAGE_USAGE_STATS

<!-- Almacenamiento -->
android.permission.READ_EXTERNAL_STORAGE
android.permission.WRITE_EXTERNAL_STORAGE

<!-- Red -->
android.permission.ACCESS_NETWORK_STATE
android.permission.INTERNET

<!-- Shizuku -->
moe.shizuku.privileged.api.BIND_SHIZUKU_SERVICE

<!-- Batería -->
android.permission.BATTERY_STATS

<!-- Notificaciones -->
android.permission.POST_NOTIFICATIONS
```

## 📝 Estructura del Proyecto

### Modelos de Datos
- `AppModel` - Información de apps
- `PerformanceStats` - Métricas de rendimiento
- `OptimizationProfile` - Perfiles de optimización
- `GameHistory` - Historial de sesiones
- `CacheData` - Datos de caché
- `NotificationData` - Notificaciones

### ViewModels
- `PerformanceMonitorViewModel` - Lógica del monitor
- `GameLauncherViewModel` - Gestión de juegos
- `MemoryCleanupViewModel` - Limpieza de memoria
- `OptimizationProfileViewModel` - Perfiles

## 🐛 Troubleshooting

### La app no detecta juegos
- Verifica que los juegos estén instalados correctamente
- Reinicia la app
- Limpia la caché: Configuración → Apps → Gamaspace → Almacenamiento → Limpiar caché

### Shizuku no funciona
- Instala Shizuku desde Play Store
- Ejecuta el comando de inicialización desde ADB
- Autoriza Gamaspace en la app de Shizuku

### Bajo rendimiento en el monitor
- Reduce la frecuencia de actualizaciones
- Limpia la caché de datos
- Verifica que no haya apps de fondo consumiendo recursos

## 📚 Documentación Técnica

### Servicio de Monitoreo
El `PerformanceMonitorService` recopila datos cada 2 segundos:
- Lee `/proc/stat` para CPU
- Usa `Runtime.getRuntime()` para RAM
- Lee estado de batería del BroadcastReceiver
- Estima GPU desde memoria nativa

### Integración Shizuku
El `ShizukuService` permite:
- Forzar detención de apps
- Limpiar caché del sistema
- Ajustar gobernadores de CPU
- Leer estadísticas avanzadas

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! 

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📜 Licencia

Este proyecto está bajo la licencia MIT. Ver `LICENSE` para más detalles.

## 📞 Contacto

- **GitHub**: [@coco0081](https://github.com/coco0081)
- **Repositorio**: [Gamaspace](https://github.com/coco0081/Gamaspace)

## 🎯 Roadmap

- [ ] Sincronización en la nube
- [ ] Análisis de FPS en juegos
- [ ] Grabación de gameplay
- [ ] Estadísticas avanzadas
- [ ] Tema claro
- [ ] Notificaciones personalizadas
- [ ] Soporte para múltiples idiomas
- [ ] Widget de escritorio

## ⭐ Agradecimientos

- Material Design 3
- Jetpack Compose
- Shizuku Project
- Android Development Community

---

**Hecho con ❤️ para gamers que quieren el mejor rendimiento**
