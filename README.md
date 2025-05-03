# Viridis - Tu Asistente de Jardinería 🌱

[![Demo Video](https://img.youtube.com/vi/1W_Ty0POHUS7qWIYmyA85kkLY0IWFm4qT/0.jpg)](https://drive.google.com/file/d/1W_Ty0POHUS7qWIYmyA85kkLY0IWFm4q

## Tabla de Contenidos
- [Introducción](#introducción)
- [Sobre el Proyecto](#sobre-el-proyecto)
- [Propósito](#propósito)
- [Tecnologías](#tecnologías)
- [Entorno de Desarrollo](#entorno-de-desarrollo)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Características](#características)
- [Instalación](#instalación)
- [Uso](#uso)
- [Contribución](#contribución)
- [Licencia](#licencia)

## Introducción
Zero es una aplicación móvil que revoluciona la forma en que cuidamos nuestras plantas. Mediante el uso de inteligencia artificial y datos meteorológicos en tiempo real, proporciona una experiencia personalizada para el cuidado de plantas.

![Capturas de pantalla](https://firebasestorage.googleapis.com/v0/b/zero-bc5b6.firebasestorage.app/o/ChatGPT%20Image%20May%202%2C%202025%2C%2006_24_02%20PM.png?alt=media&token=7000de99-d4b7-4e0f-bfca-20781d179c86)

## Sobre el Proyecto
Zero nace de la necesidad de simplificar el cuidado de plantas para usuarios principiantes y expertos. La aplicación combina tecnología de reconocimiento de imágenes con datos meteorológicos para ofrecer recomendaciones precisas y personalizadas.

## Propósito
- Facilitar la identificación de plantas
- Proporcionar consejos de cuidado personalizados basados en condiciones climáticas
- Crear una comunidad de amantes de las plantas
- Hacer la jardinería más accesible para todos

## Tecnologías
### Frontend
- Kotlin 1.8.0
- Jetpack Compose
- Material Design 3
- Navigation Component

### Backend
- Firebase Authentication
- Cloud Firestore
- Room Database
- Retrofit 2.9.0

### APIs y Servicios
- OpenWeather API
- Plant.id API
- Firebase Cloud Messaging

## Entorno de Desarrollo
### Requisitos Previos
- Android Studio Arctic Fox o superior
- JDK 11+
- Android SDK 29+
- Git

### Configuración
```bash
# Instalar Android Studio
sudo snap install android-studio --classic

# Instalar JDK 11
sudo apt install openjdk-11-jdk

# Configurar Android SDK
sdkmanager "platforms;android-29" "build-tools;30.0.3"
```

## Estructura del Proyecto
```
zero/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/zero/
│   │   │   │   ├── views/         # Interfaces de usuario
│   │   │   │   │   ├── auth/      # Autenticación
│   │   │   │   │   ├── plant/     # Gestión de plantas
│   │   │   │   │   └── social/    # Comunidad
│   │   │   │   ├── data/         # Capa de datos
│   │   │   │   ├── domain/       # Lógica de negocio
│   │   │   │   └── ml/           # Reconocimiento de plantas
│   │   │   └── res/             # Recursos
│   └── build.gradle.kts
```

## Características

### Implementadas ✅
- Autenticación de usuarios
- Reconocimiento de plantas
- Integración con datos climáticos


### En Desarrollo 🚧
- Modo offline
- Gamificación
- Recordatorios personalizados
- Integración con sensores IoT
- Sistema de comunidad y foro
- Gestión de colección de plantas
- Recomendaciones personalizadas
- 
## Instalación

1. Clonar el repositorio
```bash
git clone https://github.com/EgosPWD/Zero.git
```

2. Configurar Firebase:
   - Crear proyecto en Firebase Console
   - Añadir `google-services.json`
   - Habilitar Authentication y Firestore

3. Configurar APIs:
   - Añadir clave de API del clima en `local.properties`
   - Configurar API de reconocimiento de plantas

4. Ejecutar la aplicación

## Uso
1. Registrarse o iniciar sesión
2. Tomar foto de una planta o seleccionar de la galería
3. Recibir identificación y recomendaciones
4. Guardar en tu colección
5. Participar en la comunidad

## Contribución
1. Fork del repositorio
2. Crear rama para nueva función
3. Commit y push de cambios
4. Crear Pull Request

## Licencia
Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

---
Desarrollado con ❤️ por el equipo Zero

[⬆ Volver arriba](#tabla-de-contenidos)
