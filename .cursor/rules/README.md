# Cursor Rules - LexSign Project

Esta carpeta contiene reglas que proporcionan contexto automático al asistente de IA de Cursor.

## Reglas Disponibles

### 1. `lexsign-context.mdc` ⭐ (Always Apply)
**Carga:** Siempre, en cada conversación

Proporciona el contexto principal del proyecto:
- Arquitectura cloud-first (solo Supabase PostgreSQL)
- Stack tecnológico (Supabase, Ktor, Compose)
- Estructura del proyecto
- Patrones de código (Repository, ViewModel)
- Comandos de build y Supabase CLI
- Datos de prueba

### 2. `kotlin-standards.mdc`
**Carga:** Cuando se abren archivos `*.kt`

Estándares de código Kotlin:
- Coroutines y async patterns
- Repository pattern con suspend functions
- ViewModel pattern con StateFlow
- Error handling con RestException
- Null safety
- Naming conventions

### 3. `supabase-schema-conventions.mdc`
**Carga:** Cuando se abren archivos `supabase/schemas/**/*.sql`

Convenciones de schema SQL con Supabase:
- Schema declarativo vs migraciones
- UUIDs y tipos PostgreSQL
- Row Level Security (RLS)
- Triggers, índices, constraints
- Workflow con `supabase db diff` y `db push`

### 4. `supabase-repository-patterns.mdc`
**Carga:** Cuando se abren archivos `*Repository.kt`

Patrones para Repositories con supabase-kt:
- CRUD operations con postgrest
- Filtros y queries complejas
- Joins y relaciones
- RPC (llamadas a funciones remotas)
- Subscriptions (Realtime)
- Manejo de errores

### 5. `data-models-serialization.mdc`
**Carga:** Cuando se abren archivos `**/model/**/*.kt`

Modelos de datos con Kotlinx Serialization:
- @Serializable data classes
- Mapeo con @SerialName (snake_case)
- IDs opcionales (generados por DB)
- Timestamps, enums, nested objects
- Custom serializers
- Null safety

### 6. `compose-ui-standards.mdc`
**Carga:** Cuando se abren archivos `*.kt`

Estándares UI con Compose Multiplatform:
- Screen structure (callbacks, no NavController)
- State management y hoisting
- Loading states y error handling
- Modifiers en orden correcto
- Componentes reutilizables
- Navigation patterns
- Preview usage

### 7. `continuous-learning-system.mdc` ⭐ (Always Apply)
**Carga:** Siempre, en cada conversación

Sistema de aprendizaje continuo y testing:
- Workflow por fases (planificación → ejecución → aprendizaje)
- Testing/builds obligatorios después de cada fase
- Documentación automática en `.cursor/memory/`
- Análisis de errores y soluciones
- Aprendizaje acumulativo en `learnings.md`
- Métricas de mejora continua

## Cómo Funcionan

### Always Apply Rules
Las reglas con `alwaysApply: true` se cargan automáticamente en **cada conversación** con el asistente.

**Ejemplo:** `lexsign-context.mdc`
```yaml
---
description: Contexto principal del proyecto
alwaysApply: true
---
```

### File-Specific Rules
Las reglas con `globs` se cargan solo cuando se abren archivos que coinciden con el patrón.

**Ejemplo:** `kotlin-standards.mdc`
```yaml
---
description: Estándares de código Kotlin
globs: **/*.kt
alwaysApply: false
---
```

## Cómo Agregar Nuevas Reglas

1. **Crear archivo `.mdc` en esta carpeta**
   ```bash
   touch .cursor/rules/mi-nueva-regla.mdc
   ```

2. **Agregar frontmatter YAML**
   ```markdown
   ---
   description: Descripción breve de la regla
   globs: **/*.kt  # Opcional: patrón de archivos
   alwaysApply: false  # true para siempre cargar
   ---
   ```

3. **Escribir el contenido de la regla**
   - Usar ejemplos concretos (✅ GOOD / ❌ BAD)
   - Mantener la regla concisa (<50 líneas idealmente)
   - Enfocarse en un solo tema
   - Ser accionable y específico

## Tips

- **Concisión:** Las reglas deben ser breves y enfocadas
- **Ejemplos:** Siempre incluir ejemplos de código
- **Formato:** Usar ✅/❌ para claridad visual
- **Scope:** Una preocupación por regla
- **Límite:** Mantener bajo 500 líneas por regla

## Verificar Reglas Activas

Para ver qué reglas están activas en Cursor:
1. Abrir Command Palette (Ctrl/Cmd + Shift + P)
2. Buscar "Cursor: Show Active Rules"
3. Ver lista de reglas cargadas en la conversación actual

## Estructura de Archivos

```
.cursor/rules/
├── README.md                           # Este archivo
├── lexsign-context.mdc                 # ⭐ Always apply (contexto general)
├── continuous-learning-system.mdc      # ⭐ Always apply (testing y aprendizaje)
├── kotlin-standards.mdc                # Para *.kt
├── supabase-schema-conventions.mdc     # Para supabase/schemas/**/*.sql
├── supabase-repository-patterns.mdc    # Para *Repository.kt
├── data-models-serialization.mdc       # Para **/model/**/*.kt
└── compose-ui-standards.mdc            # Para *.kt (UI)
```

## Recursos

- [Documentación oficial de Cursor Rules](https://docs.cursor.com/context/rules-for-ai)
- [Create Rule Skill](C:\Users\migue\.cursor\skills-cursor\create-rule\SKILL.md)

---

**Última actualización:** 16 de marzo de 2026
