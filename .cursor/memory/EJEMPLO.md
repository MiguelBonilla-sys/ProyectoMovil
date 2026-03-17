# Ejemplo Práctico: Sistema de Aprendizaje Continuo

Este ejemplo muestra cómo funciona el sistema paso a paso.

## Escenario

**Usuario dice:** "Crea un módulo completo para gestionar empleados con CRUD"

---

## 1. Planificación (Lo que haré)

### Crear archivo de memoria
```
Archivo: .cursor/memory/2026-03-16_19-00_modulo-empleados-crud.md
Basado en: TEMPLATE.md
```

### Definir fases
1. **Fase 1:** Crear schema SQL en Supabase
2. **Fase 2:** Crear modelo Empleado.kt
3. **Fase 3:** Crear EmpleadoRepository
4. **Fase 4:** Crear EmpleadoViewModel
5. **Fase 5:** Actualizar memoria y learnings

---

## 2. Ejecución Fase por Fase

### FASE 1: Schema SQL

#### Acción
Crear `supabase/schemas/empleados.sql`:
```sql
create table public.empleados (
  id uuid primary key default gen_random_uuid(),
  identificacion text not null unique,
  nombre text not null,
  apellidos text not null,
  email text not null unique,
  cargo text not null,
  sueldo_devengado numeric(12,2) not null,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.empleados enable row level security;
```

#### Documentar en memoria
```markdown
### Fase 1: Crear schema SQL
**Inicio:** 19:05
**Archivos creados:**
- `supabase/schemas/empleados.sql` - Tabla empleados con RLS
```

#### TEST Obligatorio
```bash
# Generar migración
supabase db diff --schema public -f crear_empleados

# Aplicar (dry run primero)
supabase db push --dry-run
supabase db push
```

#### Documentar resultado
```markdown
**Test ejecutado:**
```bash
supabase db push
```

**Resultado:** ✅ Exitoso
**Duración:** 3 segundos
**Fin:** 19:08
```

---

### FASE 2: Modelo Empleado.kt

#### Acción
Crear `data/model/Empleado.kt`:
```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Empleado(
    val id: String? = null,
    val identificacion: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val cargo: String,
    @SerialName("sueldo_devengado")
    val sueldoDevengado: Double,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
```

#### Documentar en memoria
```markdown
### Fase 2: Crear modelo
**Inicio:** 19:09
**Archivos creados:**
- `data/model/Empleado.kt` - Data class con @Serializable
```

#### TEST Obligatorio
```bash
.\gradlew.bat :composeApp:build
```

#### Resultado - ERROR ❌
```
error: unresolved reference: SerialName
```

#### Documentar ERROR
```markdown
**Test ejecutado:**
```bash
.\gradlew.bat :composeApp:build
```

**Resultado:** ❌ Fallido
**Error:**
```
error: unresolved reference: SerialName
```

**Análisis del error:**
- **Tipo:** Compilación
- **Causa raíz:** Falta import de kotlinx.serialization.SerialName
- **Archivo afectado:** data/model/Empleado.kt
- **Línea:** 3
```

#### Corregir
```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName  // ← AGREGADO

@Serializable
data class Empleado(
    // ... resto igual
)
```

#### RE-TEST
```bash
.\gradlew.bat :composeApp:build
```

#### Documentar corrección
```markdown
**Solución aplicada:**
1. Agregar import: `import kotlinx.serialization.SerialName`

**Re-test:**
```bash
.\gradlew.bat :composeApp:build
```

**Resultado:** ✅ Exitoso
**Fin:** 19:12
**Duración fase:** 3 minutos
```

---

### FASE 3: EmpleadoRepository

#### Acción
Crear `data/repository/EmpleadoRepository.kt`:
```kotlin
class EmpleadoRepository {
    suspend fun obtenerTodos(): List<Empleado> =
        supabase.postgrest["empleados"]
            .select()
            .decodeList<Empleado>()
    
    suspend fun obtenerPorId(id: String): Empleado? =
        supabase.postgrest["empleados"]
            .select { filter { Empleado::id eq id } }
            .decodeSingleOrNull<Empleado>()
    
    suspend fun insertar(empleado: Empleado): Empleado =
        supabase.postgrest["empleados"]
            .insert(empleado) { select() }
            .decodeSingle<Empleado>()
    
    suspend fun actualizar(id: String, empleado: Empleado) {
        supabase.postgrest["empleados"]
            .update(empleado) { filter { Empleado::id eq id } }
    }
    
    suspend fun eliminar(id: String) {
        supabase.postgrest["empleados"]
            .delete { filter { Empleado::id eq id } }
    }
}
```

#### TEST
```bash
.\gradlew.bat :composeApp:build
```

#### Resultado
```markdown
**Resultado:** ✅ Exitoso (primer intento)
**Fin:** 19:16
```

---

### FASE 4: EmpleadoViewModel

[Similar a fases anteriores...]

---

## 3. Actualizar learnings.md

Después de completar TODAS las fases:

```markdown
## 📚 Patrones Exitosos

### Repository CRUD con Supabase
**Aprendido:** 2026-03-16
**Frecuencia:** 1 vez (primera implementación)
**Contexto:** Módulo de empleados
**Patrón:**
```kotlin
suspend fun obtenerTodos(): List<T> = 
    supabase.postgrest["tabla"].select().decodeList()
```
**Razón del éxito:** Directo, sin abstracciones innecesarias
**Aplicable a:** Cualquier entidad con CRUD básico

---

## ⚠️ Errores Comunes y Soluciones

### Error: unresolved reference SerialName
**Frecuencia:** 1 vez
**Contexto:** Data classes con @SerialName
**Causa:** Olvidar import de kotlinx.serialization.SerialName
**Solución:**
```kotlin
import kotlinx.serialization.SerialName
```
**Prevención:** Agregar a checklist pre-creación de modelos
**Tiempo de resolución:** 1 minuto

---

## 📊 Métricas de Aprendizaje

**Total de tareas documentadas:** 1
**Total de fases ejecutadas:** 5
**Total de errores encontrados:** 1
**Total de errores resueltos:** 1
**Total de builds exitosos:** 4
**Total de builds fallidos:** 1
**Tasa de éxito en primer intento:** 80%

### Errores más frecuentes
1. Import faltante (SerialName) - 1 vez

### Tiempo promedio por tipo de tarea
- Crear Schema SQL: 3 minutos
- Crear Modelo: 3 minutos (con corrección)
- Crear Repository: 4 minutos
- TOTAL: 10 minutos para CRUD completo
```

---

## 4. Beneficio Inmediato

La **PRÓXIMA** vez que cree un módulo similar:

### Antes del sistema
```
1. Crear modelo
2. Build falla - "unresolved reference SerialName"
3. Buscar en internet por qué falla
4. Probar soluciones
5. Finalmente agregar import
Tiempo: ~10 minutos perdidos
```

### Con el sistema
```
1. Consultar learnings.md
2. Ver: "Import de SerialName necesario"
3. Agregar import desde el inicio
4. Build exitoso en primer intento
Tiempo: 0 minutos perdidos ✅
```

---

## 5. Aprendizaje Acumulativo

Después de 10 tareas similares:

```markdown
### Error: Import faltante (SerialName)
**Frecuencia:** 8 de 10 tareas
**Tendencia:** Disminuyendo (80% → 20% después de documentar)
**Prevención efectiva:** Checklist de imports antes de build
```

El sistema **aprende** que este error es muy común y lo previene.

---

## 6. Ejemplo de Consulta Futura

**Usuario:** "Crea un Repository para gestionar consultas legales"

**Yo (Claude):**
```
[Reviso learnings.md]
[Encuentro: "Repository CRUD con Supabase - patrón exitoso"]
[Encuentro: "Import SerialName - error común"]

[Creo Repository usando patrón conocido]
[Incluyo import de SerialName desde el inicio]
[Build exitoso en primer intento] ✅
```

---

## Resumen del Flujo

```
Usuario solicita tarea
    ↓
Crear memoria/YYYY-MM-DD_HH-mm_tarea.md
    ↓
Planificar fases
    ↓
PARA CADA FASE:
    ├─ Ejecutar
    ├─ Documentar
    ├─ TEST/BUILD ← OBLIGATORIO
    ├─ ¿Exitoso?
    │   ├─ SÍ → Siguiente fase
    │   └─ NO → Analizar → Corregir → Re-test → Documentar
    ↓
Actualizar learnings.md
    ↓
Sistema aprende para próxima vez ✅
```

---

**Conclusión:** Cada error se comete UNA sola vez. Después se previene automáticamente.
