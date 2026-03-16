# Proyecto Kotlin Multiplatform - Contexto para el Asistente

## Visión General
Este es un proyecto Kotlin Multiplatform (KMP) que tiene como objetivo compartir código entre plataformas Android e iOS utilizando Jetpack Compose para la UI.

## Estructura del Proyecto

### composeApp/
Contiene el código compartido que se compila para múltiples plataformas:
- `commonMain/kotlin`: Código común para todas las plataformas (Android, iOS, Desktop, Web)
- `iosMain/kotlin`: Código específico para iOS
- `androidMain/kotlin`: Código específico para Android
- `jvmMain/kotlin`: Código específico para Desktop (JVM)
- `jsMain/kotlin`: Código específico para Web (JavaScript)

### iosApp/
Contiene la aplicación nativa de iOS que sirve como punto de entrada para la plataforma iOS. Incluso cuando se comparte la UI con Compose Multiplatform, se necesita este punto de entrada.

## Archivos de Configuración Importantes

### build.gradle.kts
Archivo de configuración de Gradle a nivel de proyecto que define:
- Versiones de Kotlin y plugins
- Configuraciones de targets (android, ios, etc.)
- Dependencias compartidas

### gradle/libs.versions.toml
Catalogue de versiones de dependencias (Versión Catálogos de Gradle) que centraliza la gestión de versiones de bibliotecas.

### settings.gradle.kts
Define los módulos incluidos en el build (composeApp, iosApp, etc.)

### local.properties
Configuración local del SDK de Android (no se debe versionar).

### gradle.properties
Propiedades de Gradle como versión de Kotlin, organización, etc.

## Instrucciones de Build y Ejecución

### Android
```bash
# En Windows
.\gradlew.bat :composeApp:assembleDebug

# En macOS/Linux
./gradlew :composeApp:assembleDebug
```

### iOS
Abrir el proyecto en Xcode ubicado en `iosApp/iosApp.xcodeproj` y ejecutar desde allí, o usar la configuración de run en el IDE.

## Puntos de Entrada

### Android
El punto de entrada se encuentra en `composeApp/src/androidMain/kotlin` (generalmente una Activity que configura Compose).

### iOS
El punto de entrada está en `iosApp/iosApp/iOSApp.swift` (SwiftUI App que hosts la vista de Compose).

## Tecnologías Utilizadas
- Kotlin Multiplatform
- Jetpack Compose (para UI compartida)
- Gradle con Version Catalogs
- Xcode para build iOS
- Android Studio/IntelliJ para desarrollo

## Notas Importantes
1. El código compartido en `commonMain` puede acceder a APIs específicas de plataforma mediante expect/actual.
2. Los recursos específicos de plataforma (como imágenes, strings) deben colocarse en los sourceSets correspondientes.
3. Para acceder a APIs nativas de iOS desde el código compartido, se utilizan las declaraciones `expect` en commonMain y las implementaciones `actual` en iosMain.
4. El proyecto utiliza el plugin de Kotlin Multiplatform de Gradle para configurar los targets.