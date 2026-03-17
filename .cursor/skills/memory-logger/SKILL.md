---
name: memory-logger
description: Creates and updates memory log files for tasks in .cursor/memory/. Use when starting a new multi-phase task, documenting a completed phase, logging a build error and its fix, or updating the learnings.md file with new patterns or anti-patterns discovered.
---

# Memory Logger

Crea y actualiza archivos de memoria en `.cursor/memory/` para documentar tareas, errores y aprendizajes.

## Crear archivo de tarea nueva

Al iniciar una tarea compleja, crear archivo basado en el template:

```
Archivo: .cursor/memory/YYYY-MM-DD_HH-mm_nombre-descriptivo.md
Template base: .cursor/memory/TEMPLATE.md
```

**Pasos:**
1. Leer `.cursor/memory/TEMPLATE.md`
2. Crear nuevo archivo con fecha/hora real del sistema
3. Rellenar: nombre de tarea, objetivo, fases del plan

**Nombre del archivo:**
```
# Obtener fecha/hora
$now = Get-Date -Format "yyyy-MM-dd_HH-mm"
# Ejemplo: 2026-03-16_19-30_autenticacion-completa.md
```

## Documentar fase completada

Después de ejecutar una fase, agregar al archivo de memoria activo:

```markdown
### Fase N: [NOMBRE]
**Inicio:** HH:mm
**Archivos modificados:**
- `ruta/archivo.kt` - descripción del cambio

**Comando de verificación:**
```bash
[comando ejecutado]
```

**Resultado:** ✅ Exitoso | ❌ Fallido
**Fin:** HH:mm
```

## Documentar error y su corrección

Cuando un build/test falla:

```markdown
**Resultado:** ❌ Fallido
**Error:**
```
[error completo copiado]
```

**Análisis:**
- Tipo: [Compilación | Runtime | Sintaxis]
- Causa: [explicación]
- Archivo: `ruta/archivo.kt` línea X

**Corrección:**
- Cambio: [descripción]

**Re-test:** ✅ Exitoso
```

## Actualizar learnings.md

Al finalizar una tarea, agregar aprendizajes al archivo acumulativo:

### Nuevo patrón exitoso
```markdown
### [Nombre del patrón]
**Aprendido:** YYYY-MM-DD
**Contexto:** [tarea donde se aplicó]
**Patrón:**
```kotlin
// código del patrón
```
**Por qué funciona:** [explicación]
```

### Nuevo error documentado
```markdown
### Error: [nombre descriptivo]
**Frecuencia:** X vez/veces
**Causa:** [causa raíz]
**Solución:**
```kotlin
// fix
```
**Prevención:** [cómo evitarlo]
```

### Actualizar métricas
Incrementar en `learnings.md`:
- Total de tareas documentadas
- Total de errores encontrados/resueltos
- Builds exitosos/fallidos

## Consultar memoria antes de una tarea

Antes de empezar tarea similar a una pasada:
1. Leer `learnings.md` → buscar patrones y errores relacionados
2. Buscar archivos de memoria relevantes con Grep
3. Aplicar aprendizajes previos proactivamente

## Referencia de archivos
- Template: `.cursor/memory/TEMPLATE.md`
- Aprendizajes: `.cursor/memory/learnings.md`
- Ejemplo: `.cursor/memory/EJEMPLO.md`
