# Tarea: LexSign MVP Implementation — Cloud-First Supabase
**Fecha de inicio:** 2026-03-16 19:00
**Fecha de completado:** 2026-03-16 21:00
**Status:** ✅ COMPLETADO

## 🎯 Objetivo
Migrar de arquitectura SQLDelight+hardcoded a cloud-first Supabase completa cubriendo RF1-RF13 en 4 fases.

## ✅ Resultado Final
BUILD SUCCESSFUL — APK Android debug compila sin errores.

## 📋 Fases Completadas

### Fase 1 — Fundación y Seguridad
- [x] 1.1 Limpieza SQLDelight: DatabaseDriverFactory, AppContainer, FileStorage, User.sq eliminados
- [x] 1.2 SupabaseClientProvider actualizado + top-level `val supabase`
- [x] 1.3 SecurityUtils.kt — PBKDF2 SHA-256, 10k iteraciones (expect/actual para Android/iOS)
- [x] 1.4 User.kt extendido con campos RF1 (preferenciasAreas, tipoTramites) y RF2 (especialidad, experiencia, etc.)
- [x] 1.5 AuthRepository (cloud-first) + AuthViewModel extiende ViewModel real con StateFlow

### Fase 2 — Schemas y Entidades Core
- [x] 2.1 supabase/schemas/01_users.sql con RLS
- [x] 2.2 supabase/schemas/02_consultas.sql + tabla consulta_abogados
- [x] 2.3 supabase/schemas/03_documentos.sql + Storage buckets
- [x] 2.4 Consulta.kt + Documento.kt con enums EstadoConsulta y EstadoFirma
- [x] 2.5 ConsultaRepository.kt + DocumentoRepository.kt con CRUD completo

### Fase 3 — UI Conectada
- [x] 3.1 AbogadoViewModel + LawyersScreen con búsqueda y filtros reales
- [x] 3.2 ConsultaViewModel + ConsultationsScreen + diálogo nueva consulta
- [x] 3.3 DocumentoViewModel + DocumentsScreen con plantillas
- [x] 3.4 HomeViewModel + HomeScreen con consultas recientes reales
- [x] 3.5 ProfileViewModel + ProfileScreen con edición real

### Fase 4 — Funcionalidades Avanzadas
- [x] 4.1 supabase/schemas/04_notificaciones.sql + columna rol en users
- [x] 4.2 NotificacionRepository + NotificacionViewModel con Realtime
- [x] 4.3 RegisterAbogadoScreen con campos profesionales
- [x] 4.4 ReporteRepository + ReportesScreen + supabase/schemas/05_vistas_reportes.sql

## 🔧 Correcciones aplicadas
1. `@file:Suppress("DEPRECATION")` en build.gradle.kts para Kotlin 2.3 + AGP 9.0
2. Removido SQLDelight de build.gradle.kts raíz y composeApp
3. Removido BOM platform() deprecated — supabase-kt ya tiene versiones explícitas
4. `@OptIn(ExperimentalMaterial3Api::class)` en ExposedDropdownMenuBox
5. NotificacionViewModel — no usar `filter` privado del postgresChangeFlow
6. `@Suppress("DEPRECATION")` inline en `compose.materialIconsExtended`
7. iOS build falla con KLIB duplicados (pre-existente, no blocking para Android)

## 📁 Archivos nuevos/modificados
```
composeApp/src/commonMain/kotlin/.../
  data/model/User.kt                     (extendido)
  data/model/Consulta.kt                 (nuevo)
  data/model/Documento.kt                (nuevo)
  data/model/Notificacion.kt             (nuevo)
  data/remote/SupabaseClientProvider.kt  (actualizado + val supabase)
  data/repository/AuthRepository.kt      (nuevo, reemplaza UserRepository)
  data/repository/ConsultaRepository.kt  (nuevo)
  data/repository/DocumentoRepository.kt (nuevo)
  data/repository/NotificacionRepository.kt (nuevo)
  data/repository/ReporteRepository.kt   (nuevo)
  data/util/SecurityUtils.kt             (nuevo, expect)
  ui/viewmodel/AuthViewModel.kt          (refactorizado -> ViewModel real)
  ui/viewmodel/AbogadoViewModel.kt       (nuevo)
  ui/viewmodel/ConsultaViewModel.kt      (nuevo)
  ui/viewmodel/DocumentoViewModel.kt     (nuevo)
  ui/viewmodel/HomeViewModel.kt          (nuevo)
  ui/viewmodel/ProfileViewModel.kt       (nuevo)
  ui/viewmodel/NotificacionViewModel.kt  (nuevo)
  ui/screens/LawyersScreen.kt            (conectado a Supabase)
  ui/screens/ConsultationsScreen.kt      (conectado a Supabase)
  ui/screens/DocumentsScreen.kt          (conectado a Supabase)
  ui/screens/HomeScreen.kt               (conectado a Supabase)
  ui/screens/ProfileScreen.kt            (edición real)
  ui/screens/RegisterAbogadoScreen.kt    (nuevo)
  ui/screens/ReportesScreen.kt           (nuevo)
  ui/screens/MainScreen.kt               (roles + notif badge)
  navigation/NavGraph.kt                 (añadido REGISTER_ABOGADO)
  App.kt                                 (limpiado)
androidMain/data/util/SecurityUtils.kt   (nuevo, actual PBKDF2)
iosMain/data/util/SecurityUtils.kt       (nuevo, actual CommonCrypto)
supabase/schemas/01_users.sql            (nuevo)
supabase/schemas/02_consultas.sql        (nuevo)
supabase/schemas/03_documentos.sql       (nuevo)
supabase/schemas/04_notificaciones.sql   (nuevo)
supabase/schemas/05_vistas_reportes.sql  (nuevo)
```

## 📌 Notas para el desarrollador
1. Crear archivo `.env` con `SUPABASE_URL` y `SUPABASE_ANON_KEY` reales
2. Ejecutar schemas en Supabase Dashboard > SQL Editor en orden 01→05
3. Crear buckets "documentos" y "plantillas" en Supabase Storage
4. Conectar supabase-cli con `supabase link --project-ref <ref>`
