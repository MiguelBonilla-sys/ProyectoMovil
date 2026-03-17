---
name: generate-supabase-model
description: Generates a complete @Serializable Kotlin data class that maps to a Supabase PostgreSQL table for the LexSign project. Use when the user asks to create a model, data class, entity, or Kotlin representation of a database table. Produces correctly annotated classes with @SerialName mappings, nullable IDs, optional timestamps, and proper enum handling.
---

# Generate Supabase Model

Genera un `@Serializable data class` que mapea exactamente una tabla PostgreSQL de Supabase.

## Información necesaria

Antes de generar, determinar:
1. **Nombre de la tabla** en PostgreSQL (ej: `empleados`)
2. **Columnas y tipos** de la tabla
3. **¿Hay enums?** (columnas con valores fijos)
4. **¿Hay relaciones?** (foreign keys que se hacen join)

Si el usuario no especificó las columnas, preguntar o inferir del schema en `supabase/schemas/`.

## Proceso de generación

### Paso 1: Leer schema existente (si aplica)
```
Buscar: supabase/schemas/<tabla>.sql
Si existe → leer columnas y tipos
Si no existe → usar columnas indicadas por el usuario
```

### Paso 2: Mapear tipos PostgreSQL → Kotlin

| PostgreSQL | Kotlin | Nota |
|-----------|--------|------|
| `uuid` | `String?` | Nullable — generado por DB |
| `text` | `String` | Non-null si NOT NULL en DB |
| `text` (nullable) | `String?` | Cuando columna es nullable |
| `numeric(12,2)` | `Double` | O `Float` si se prefiere |
| `integer` | `Int` | |
| `bigint` | `Long` | |
| `boolean` | `Boolean` | |
| `timestamptz` | `String?` | ISO 8601, nullable si generado por DB |
| `enum type` | `Kotlin enum` | Crear enum `@Serializable` separado |
| `uuid` FK | `String?` | ID referencia otra tabla |

### Paso 3: Aplicar reglas de naming

- **Clase Kotlin:** PascalCase singular (tabla `empleados` → clase `Empleado`)
- **Campo Kotlin:** camelCase (`sueldo_devengado` → `sueldoDevengado`)
- **@SerialName:** snake_case exacto de la columna PostgreSQL
- **@SerialName solo si** el nombre difiere del campo Kotlin (siempre que sea snake_case)

### Paso 4: Estructura del modelo

```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class [NombreEntidad](
    // ID — siempre nullable (generado por PostgreSQL)
    val id: String? = null,

    // Campos requeridos (NOT NULL en DB)
    val campo: String,

    // Campos con nombre compuesto — requieren @SerialName
    @SerialName("nombre_compuesto")
    val nombreCompuesto: String,

    // Campos nullable (nullable en DB)
    val campoOpcional: String? = null,

    // Timestamps — siempre nullable con default null
    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
```

### Paso 5: Enums (si aplica)

Si hay columna con valores fijos (ej: `tipo TEXT CHECK(tipo IN ('cliente','abogado'))`):

```kotlin
@Serializable
enum class TipoUsuario {
    @SerialName("cliente")
    CLIENTE,

    @SerialName("abogado")
    ABOGADO
}
```

Usar el enum en el modelo:
```kotlin
@Serializable
data class Usuario(
    val id: String? = null,
    val nombre: String,
    val tipo: TipoUsuario
)
```

### Paso 6: Relaciones (joins)

Si hay foreign key y se necesita el objeto anidado:

```kotlin
// Tabla principal
@Serializable
data class Consulta(
    val id: String? = null,
    @SerialName("cliente_id")
    val clienteId: String,
    @SerialName("abogado_id")
    val abogadoId: String,
    val descripcion: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

// Modelo con join (para queries con select("*, cliente:cliente_id(*)"))
@Serializable
data class ConsultaConCliente(
    val id: String? = null,
    val descripcion: String,
    val cliente: Usuario?    // objeto anidado del join
)
```

## Checklist antes de entregar

- [ ] Clase tiene `@Serializable`
- [ ] Imports incluyen `Serializable` y `SerialName` (si se usa)
- [ ] `id` es `String? = null`
- [ ] `created_at` y `updated_at` son `String? = null`
- [ ] Todos los campos snake_case tienen `@SerialName`
- [ ] Enums tienen `@SerialName` por valor
- [ ] Nullability refleja exactamente el schema de DB
- [ ] Nombre de clase es PascalCase singular
- [ ] Nombre de archivo es `NombreEntidad.kt` en `data/model/`

## Ejemplos reales del proyecto LexSign

### Modelo existente: Usuario
```kotlin
// data/model/Usuario.kt
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class TipoUsuario {
    @SerialName("cliente") CLIENTE,
    @SerialName("abogado") ABOGADO
}

@Serializable
data class Usuario(
    val id: String? = null,
    val nombre: String,
    val email: String,
    val password: String,          // almacenar hash PBKDF2, nunca plaintext
    val tipo: TipoUsuario,
    val tarjeta: String? = null,   // solo abogados
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
```

### Modelo a crear: Consulta
Schema `supabase/schemas/consultas.sql`:
```sql
create table public.consultas (
  id uuid primary key default gen_random_uuid(),
  cliente_id uuid not null references users(id),
  abogado_id uuid not null references users(id),
  descripcion text not null,
  estado text not null check(estado in ('pendiente','activa','cerrada')) default 'pendiente',
  fecha_consulta timestamptz,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);
```

Modelo generado `data/model/Consulta.kt`:
```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class EstadoConsulta {
    @SerialName("pendiente") PENDIENTE,
    @SerialName("activa") ACTIVA,
    @SerialName("cerrada") CERRADA
}

@Serializable
data class Consulta(
    val id: String? = null,
    @SerialName("cliente_id")
    val clienteId: String,
    @SerialName("abogado_id")
    val abogadoId: String,
    val descripcion: String,
    val estado: EstadoConsulta = EstadoConsulta.PENDIENTE,
    @SerialName("fecha_consulta")
    val fechaConsulta: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// Con datos del cliente/abogado (para queries con join)
@Serializable
data class ConsultaDetalle(
    val id: String? = null,
    val descripcion: String,
    val estado: EstadoConsulta,
    val cliente: Usuario? = null,
    val abogado: Usuario? = null,
    @SerialName("fecha_consulta")
    val fechaConsulta: String? = null
)
```

## Verificación post-generación

Siempre ejecutar build después de crear el modelo:
```powershell
.\gradlew.bat :composeApp:build
```

Error más común: `unresolved reference: SerialName` → agregar import.
