---
name: kmp-build-runner
description: Runs builds and tests for the LexSign Kotlin Multiplatform project on Windows and interprets results. Use when you need to verify Kotlin code compiles, run Android debug build, execute unit tests, validate Supabase schema migrations, or check for lint errors after code changes.
---

# KMP Build Runner

Ejecuta verificaciones del proyecto LexSign KMP en Windows y documenta resultados.

## Comandos por tipo de cambio

### Cambios en Kotlin (cualquier archivo .kt)
```bash
# Build principal — siempre ejecutar primero
.\gradlew.bat :composeApp:build

# Si falla, ver error detallado
.\gradlew.bat :composeApp:build --stacktrace
```

### Cambios de UI (Compose)
```bash
# Build de debug (más rápido)
.\gradlew.bat :composeApp:assembleDebug
```

### Cambios en Repository o ViewModel
```bash
# Build + tests unitarios
.\gradlew.bat :composeApp:build
.\gradlew.bat :composeApp:testDebugUnitTest
```

### Cambios en schema Supabase (.sql)
```bash
# Verificar sintaxis
supabase db lint

# Generar migración (siempre con nombre descriptivo)
supabase db diff --schema public -f nombre_del_cambio

# Dry run antes de aplicar
supabase db push --dry-run

# Aplicar si dry run es exitoso
supabase db push
```

### Verificación de linter (después de editar archivos)
Usar la herramienta `ReadLints` sobre el archivo modificado — NO sobre todo el proyecto.

## Interpretar resultados de build

### ✅ Build exitoso
```
BUILD SUCCESSFUL in Xs
```
→ Documentar en memoria como exitoso, continuar siguiente fase.

### ❌ Errores comunes y causa raíz

**Unresolved reference**
```
error: unresolved reference: NombreClase
```
Causa: import faltante o typo en nombre.
Fix: agregar import correcto.

**Serialization exception / @Serializable missing**
```
error: This class is not serializable
```
Causa: Falta `@Serializable` en data class o import.
Fix: agregar anotación y `import kotlinx.serialization.Serializable`.

**@SerialName unresolved**
```
error: unresolved reference: SerialName
```
Fix: `import kotlinx.serialization.SerialName`

**Type mismatch**
```
error: type mismatch: inferred type is X but Y was expected
```
Causa: tipo incorrecto en asignación o función.
Fix: corregir tipo o castear apropiadamente.

**Suspension function called from non-coroutine**
```
error: Suspension functions can be called only within coroutine body
```
Fix: marcar función con `suspend` o llamar desde `coroutineScope.launch`.

**StateFlow no inicializado**
```
error: Property must be initialized or be abstract
```
Fix: inicializar con `MutableStateFlow(valorInicial)`.

**Supabase: schema diff falla**
```
Error: No differences found
```
→ El schema actual ya coincide con el declarado, no se necesita migración.

**Supabase: push falla por RLS**
```
Error: new row violates row-level security policy
```
→ Revisar políticas RLS, temporalmente desactivar para desarrollo si es necesario.

## Flujo de verificación estándar

```
1. Ejecutar comando
2. ¿Exitoso?
   SÍ → documentar en memoria ✅, continuar
   NO → leer error completo
       → identificar causa (tabla de errores arriba)
       → aplicar fix
       → volver a paso 1
3. Después de 3 fallos en el mismo error:
   → Leer learnings.md para ver si hay solución documentada
   → Si no hay → investigar más profundamente
```

## Working directory

Todos los comandos `gradlew.bat` deben ejecutarse desde la raíz del proyecto:
```
C:\Users\migue\OneDrive\Documents\DEVs\Proyecto\
```

Los comandos `supabase` desde la misma raíz donde existe `supabase/`.

## Tiempos esperados

| Comando | Tiempo típico |
|---------|--------------|
| `assembleDebug` | 30-90 segundos |
| `:composeApp:build` | 60-120 segundos |
| `testDebugUnitTest` | 10-30 segundos |
| `supabase db push` | 3-10 segundos |

Si supera el doble del tiempo típico → posible cuelgue, revisar proceso.
