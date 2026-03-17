# LexSign — Contexto Persistente del Proyecto

> Este archivo sobrevive entre sesiones. Se actualiza manualmente cuando cambia la arquitectura, se agrega una entidad, o se toma una decisión técnica importante.

**Última actualización:** 2026-03-16

---

## Descripción General

**LexSign** conecta clientes con abogados para servicios legales en Colombia.

- Plataforma: Kotlin Multiplatform (Android + iOS)
- Base de datos: Supabase PostgreSQL (cloud-first, sin almacenamiento local)
- UI: Compose Multiplatform + Material 3

---

## Entidades Principales

| Entidad | Tabla Supabase | Descripción |
|---------|---------------|-------------|
| `Usuario` | `users` | Clientes y abogados (campo `tipo`) |
| `Consulta` | `consultas` | Consultas legales entre cliente y abogado |
| `Documento` | `documentos` | Documentos asociados a consultas |

### Tipos de usuario (`tipo`)
- `'cliente'` — Personas que buscan asesoría legal
- `'abogado'` — Profesionales con tarjeta profesional

---

## Autenticación

| Tipo | Campos requeridos |
|------|------------------|
| Cliente | email + password |
| Abogado | email + tarjeta profesional + password |

**Seguridad:**
- Passwords hasheados con PBKDF2 antes de almacenar (nunca plaintext)
- RLS activo en todas las tablas
- Credenciales Supabase solo en `.env` → `BuildConfig`

---

## Arquitectura de Datos

```
KMP App
  └── supabase-kt (postgrest-kt)
        └── HTTPS/REST
              └── Supabase PostgreSQL
                    ├── users
                    ├── consultas
                    └── documentos
```

**No existe:**
- ❌ SQLDelight
- ❌ SQLite / Room
- ❌ Almacenamiento local
- ❌ Sincronización / syncStatus

---

## Stack Técnico

```toml
# libs.versions.toml (valores actuales)
supabase  = "3.3.0"
ktor      = "3.1.1"
kotlin    = "2.3.0"
compose   = "1.8.0"
```

| Capa | Tecnología |
|------|-----------|
| Base de datos | Supabase PostgreSQL |
| Cliente HTTP | supabase-kt + Ktor |
| UI | Compose Multiplatform |
| Serialización | Kotlinx Serialization |
| Schema management | Supabase CLI |
| Build | Gradle + Version Catalogs |

---

## Estructura de Directorios

```
composeApp/src/commonMain/kotlin/
├── data/
│   ├── model/          ← @Serializable data classes
│   ├── repository/     ← Lógica de negocio + postgrest queries
│   └── SupabaseClient.kt  ← Singleton del cliente
├── presentation/
│   └── *ViewModel.kt   ← StateFlow, viewModelScope
├── ui/
│   ├── screens/        ← Composables (Screens)
│   └── theme/          ← MaterialTheme config
└── navigation/
    └── NavGraph.kt     ← Rutas de navegación

supabase/
├── schemas/            ← SQL declarativo (fuente de verdad)
│   ├── users.sql
│   └── ...
└── migrations/         ← Generado por 'supabase db diff'
```

---

## Pantallas Existentes

| Pantalla | Descripción |
|----------|-------------|
| `WelcomeScreen` | Pantalla inicial — elegir tipo de usuario |
| `LoginClienteScreen` | Login de clientes (email + password) |
| `LoginAbogadoScreen` | Login abogados (email + tarjeta + password) |
| `RegisterClienteScreen` | Registro de nuevos clientes |
| `MainScreen` | Pantalla principal post-login |

---

## Archivos Clave

| Archivo | Propósito |
|---------|-----------|
| `SupabaseClient.kt` | Singleton con `createSupabaseClient(...)` |
| `UserRepository.kt` | CRUD usuarios + autenticación |
| `AuthViewModel.kt` | Estado de login/registro |
| `.env` | Credenciales Supabase (NO versionar) |
| `.env.example` | Template sin credenciales (SÍ versionar) |
| `supabase/schemas/users.sql` | Schema tabla users |

---

## Políticas RLS Activas

| Tabla | Política | Descripción |
|-------|---------|-------------|
| `users` | `"Ver propio perfil"` | SELECT solo para auth.uid() = id |
| `users` | `"Actualizar propio perfil"` | UPDATE solo para auth.uid() = id |

---

## Datos de Prueba (Seed)

```
Clientes:
  cliente1@example.com / password123
  cliente2@example.com / password123

Abogados:
  abogado1@example.com / TARJ-001 / password123
  abogado2@example.com / TARJ-002 / password123
  abogado3@example.com / TARJ-003 / password123
```

---

## Decisiones Técnicas Importantes

### 2026-03-16 — Cloud-First (sin SQLDelight)
**Decisión:** No usar SQLDelight ni almacenamiento local.
**Razón:** La arquitectura del proyecto es cloud-first. Supabase es la única fuente de verdad. No hay requisito de funcionalidad offline.
**Implicación:** Repositories usan `suspend fun` que llaman directamente a postgrest. No hay `Flow<T>` desde repositorio.

### 2026-03-16 — Supabase CLI para schema management
**Decisión:** Usar Supabase CLI con schemas declarativos (`supabase/schemas/*.sql`).
**Razón:** Permite versionado del schema, generación automática de migraciones y separación entre definición y migración.
**Workflow:** Editar `.sql` → `supabase db diff -f nombre` → `supabase db push`.

### 2026-03-16 — @SerialName para todos los campos snake_case
**Decisión:** Usar `@SerialName` en todos los campos cuyo nombre difiera del nombre de columna PostgreSQL.
**Razón:** PostgreSQL usa snake_case, Kotlin usa camelCase. Sin `@SerialName`, la deserialización falla silenciosamente.
**Regla:** Cualquier campo que en DB sea `palabra_compuesta` DEBE tener `@SerialName("palabra_compuesta")`.

---

## Próximas Funcionalidades Pendientes

- [ ] Módulo de consultas (CRUD completo)
- [ ] Módulo de documentos (upload a Supabase Storage)
- [ ] Perfil de abogado con información profesional
- [ ] Búsqueda de abogados por especialidad
- [ ] Sistema de notificaciones (Supabase Realtime)
- [ ] Autenticación con Supabase Auth (reemplazar auth manual)

---

## Configuración de Supabase

**Project URL:** En `.env` como `SUPABASE_URL`
**Anon Key:** En `.env` como `SUPABASE_ANON_KEY`
**Project Ref:** Necesario para `supabase link --project-ref`

### Configurar entorno local
```bash
cp .env.example .env
# Editar .env con credenciales reales del dashboard Supabase
.\gradlew.bat :composeApp:assembleDebug
```

---

## Notas de Seguridad

- ✅ `.env` en `.gitignore` — credenciales nunca en repo
- ✅ `anon` key en app (nunca `service_role`)
- ✅ RLS activo en todas las tablas con datos de usuario
- ✅ Passwords hasheados (PBKDF2) — nunca plaintext en DB
- ✅ BuildConfig generado en build-time desde `.env`
