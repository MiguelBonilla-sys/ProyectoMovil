# Carpeta de Memoria del Proyecto

Esta carpeta contiene el **sistema de aprendizaje continuo** del proyecto LexSign. Cada tarea significativa se documenta aquí para crear un historial de aprendizaje que mejora con el tiempo.

## 📁 Estructura

```
.cursor/memory/
├── README.md                    # Este archivo
├── TEMPLATE.md                  # Template para nuevas tareas
├── learnings.md                 # Aprendizajes acumulados (principal)
├── YYYY-MM-DD_HH-mm_tarea1.md   # Documentación de tarea específica
├── YYYY-MM-DD_HH-mm_tarea2.md   # Documentación de tarea específica
└── ...
```

## 🎯 Propósito

### Documentar
Cada tarea compleja se documenta con:
- Plan original (fases)
- Ejecución paso a paso
- Errores encontrados
- Soluciones aplicadas
- Lecciones aprendidas

### Aprender
El archivo `learnings.md` acumula:
- Patrones exitosos
- Errores comunes y soluciones
- Anti-patrones detectados
- Métricas de mejora

### Mejorar
Con cada tarea documentada:
- Se identifican patrones recurrentes
- Se evitan errores ya resueltos
- Se optimiza el proceso de desarrollo
- Se construye conocimiento institucional

## 📝 Cómo Usar

### Para el Asistente (Claude)

Cuando recibo una tarea compleja:

1. **Crear archivo de memoria** usando TEMPLATE.md
   ```
   Nombre: YYYY-MM-DD_HH-mm_nombre-descriptivo.md
   Ejemplo: 2026-03-16_14-30_autenticacion-completa.md
   ```

2. **Documentar plan** con fases numeradas

3. **Ejecutar cada fase:**
   - Realizar cambios
   - Documentar cambios
   - Ejecutar build/test
   - Documentar resultado
   - Si falla: analizar, corregir, documentar

4. **Actualizar learnings.md** al finalizar

5. **Consultar learnings.md** antes de nuevas tareas similares

### Para el Usuario

Puedes:

- **Revisar tareas pasadas** - Ver cómo se resolvieron problemas similares
- **Consultar learnings.md** - Ver patrones y errores conocidos
- **Analizar métricas** - Ver progreso de aprendizaje
- **Solicitar informes** - "¿Cuántas veces hemos encontrado error X?"

## 🔍 Tipos de Documentación

### Archivo de Tarea Individual
Documenta UNA tarea específica:
- Fecha y hora
- Plan original
- Ejecución detallada por fase
- Errores y soluciones
- Lecciones aprendidas específicas

### learnings.md (Aprendizajes Acumulados)
Documenta PATRONES a través de múltiples tareas:
- Patrones exitosos recurrentes
- Errores comunes (con frecuencia)
- Anti-patrones identificados
- Métricas generales
- Recomendaciones actualizadas

## 📊 Métricas Rastreadas

- Total de tareas documentadas
- Total de errores encontrados/resueltos
- Tipos de errores más frecuentes
- Tasa de éxito en builds
- Tiempo promedio por tipo de tarea
- Patrones identificados
- Anti-patrones evitados

## 🎓 Ejemplos de Aprendizajes

### Patrón Exitoso
```markdown
### Repository con Supabase
**Frecuencia de uso:** 5 tareas
**Tasa de éxito:** 100%
**Patrón:**
```kotlin
suspend fun obtener(): List<T> = 
    supabase.postgrest["tabla"].select().decodeList()
```
**Por qué funciona:** Simple, directo, sin abstracciones innecesarias
```

### Error Común
```markdown
### Error: @SerialName faltante
**Frecuencia:** 3 veces
**Contexto:** Data classes con campos snake_case
**Causa:** Olvidar mapear columnas de PostgreSQL
**Solución:**
```kotlin
@SerialName("nombre_completo")
val nombreCompleto: String
```
**Prevención:** Checklist antes de crear modelos
```

## 🔄 Workflow Completo

```
┌─────────────────────────────────────────────────┐
│  Usuario: "Crea módulo de autenticación"       │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  1. Crear archivo memoria/2026-03-16_...md     │
│  2. Documentar plan con fases                   │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  PARA CADA FASE:                                │
│  ├─ Ejecutar cambios                            │
│  ├─ Documentar cambios                          │
│  ├─ Ejecutar build/test ✅                      │
│  ├─ Documentar resultado                        │
│  └─ Si falla:                                   │
│     ├─ Analizar error                           │
│     ├─ Documentar causa                         │
│     ├─ Aplicar corrección                       │
│     ├─ Re-ejecutar build ✅                     │
│     └─ Documentar corrección                    │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  3. Documentar lecciones aprendidas             │
│  4. Actualizar learnings.md                     │
│  5. Actualizar métricas                         │
└─────────────────────────────────────────────────┘
```

## 🚀 Beneficios Observados

Después de implementar este sistema:

1. **Menos errores repetidos** - Consultar learnings.md previene errores conocidos
2. **Debugging más rápido** - Historial de errores similares acelera solución
3. **Mejora continua** - Métricas muestran progreso real
4. **Conocimiento persistente** - No se pierde información entre sesiones
5. **Onboarding más rápido** - Nuevos desarrolladores pueden leer el historial

## 📖 Comandos Útiles

```bash
# Ver todas las tareas documentadas
ls .cursor/memory/*.md | grep -v TEMPLATE | grep -v learnings | grep -v README

# Buscar error específico en historial
grep -r "SerializationException" .cursor/memory/

# Contar tareas completadas
ls .cursor/memory/2026*.md | wc -l

# Ver patrones más recientes
tail -50 .cursor/memory/learnings.md
```

## 🔧 Mantenimiento

### Cada tarea:
- Crear archivo nuevo
- Documentar completamente
- Actualizar learnings.md

### Cada semana:
- Revisar métricas
- Identificar tendencias
- Ajustar recomendaciones

### Cada mes:
- Archivar tareas antiguas (>30 días) si la carpeta crece mucho
- Actualizar patrones consolidados
- Generar reporte de progreso

## 📚 Recursos

- **Regla:** `.cursor/rules/continuous-learning-system.mdc`
- **Template:** `TEMPLATE.md`
- **Aprendizajes:** `learnings.md`

---

**Última actualización:** 2026-03-16
**Sistema versión:** 1.0
