---
name: create-repository-method
description: Generates correct Repository methods using supabase-kt postgrest for the LexSign project. Use when the user asks to add a query, CRUD operation, filter, join, search, RPC call, or any data access method to a repository class. Produces suspend functions with proper postgrest DSL, error handling, and logging.
---

# Create Repository Method

Genera métodos `suspend fun` para repositorios del proyecto LexSign usando `supabase-kt` postgrest.

## Determinar tipo de operación

Antes de generar, identificar qué tipo es:

| Tipo | Cuándo usar |
|------|------------|
| **CREATE** | Insertar un registro nuevo |
| **READ-ALL** | Obtener lista completa |
| **READ-ONE** | Obtener uno por ID u otro campo |
| **READ-FILTER** | Obtener lista con filtros |
| **UPDATE** | Modificar un registro existente |
| **DELETE** | Eliminar un registro |
| **UPSERT** | Insert o update según si existe |
| **RPC** | Llamar función PostgreSQL |
| **JOIN** | Obtener con datos de tabla relacionada |

## Plantillas por tipo

### CREATE
```kotlin
suspend fun insertar(entidad: Entidad): Entidad =
    supabase.postgrest["tabla"]
        .insert(entidad) { select() }
        .decodeSingle<Entidad>()
```

### READ-ALL
```kotlin
suspend fun obtenerTodos(): List<Entidad> =
    supabase.postgrest["tabla"]
        .select()
        .decodeList<Entidad>()
```

### READ-ONE por ID
```kotlin
suspend fun obtenerPorId(id: String): Entidad? =
    supabase.postgrest["tabla"]
        .select {
            filter { Entidad::id eq id }
        }
        .decodeSingleOrNull<Entidad>()
```

### READ-FILTER (múltiples condiciones)
```kotlin
suspend fun buscarPorCampo(valor: String): List<Entidad> =
    supabase.postgrest["tabla"]
        .select {
            filter {
                Entidad::campo eq valor
            }
        }
        .decodeList<Entidad>()
```

### READ-FILTER con múltiples condiciones
```kotlin
suspend fun buscarPorTipoYEstado(tipo: String, estado: String): List<Entidad> =
    supabase.postgrest["tabla"]
        .select {
            filter {
                Entidad::tipo eq tipo
                Entidad::estado eq estado
            }
        }
        .decodeList<Entidad>()
```

### READ con orden y paginación
```kotlin
suspend fun obtenerPaginado(desde: Int, hasta: Int): List<Entidad> =
    supabase.postgrest["tabla"]
        .select {
            order(column = "created_at", ascending = false)
            range(desde.toLong(), hasta.toLong())
        }
        .decodeList<Entidad>()
```

### READ búsqueda de texto (ILIKE)
```kotlin
suspend fun buscarPorNombre(query: String): List<Entidad> =
    supabase.postgrest["tabla"]
        .select {
            filter {
                Entidad::nombre ilike "%$query%"
            }
        }
        .decodeList<Entidad>()
```

### UPDATE
```kotlin
suspend fun actualizar(id: String, entidad: Entidad) {
    supabase.postgrest["tabla"]
        .update(entidad) {
            filter { Entidad::id eq id }
        }
}
```

### UPDATE campos específicos (evitar sobreescribir todo)
```kotlin
suspend fun actualizarEstado(id: String, nuevoEstado: String) {
    supabase.postgrest["tabla"]
        .update({
            set("estado", nuevoEstado)
            set("updated_at", "now()")
        }) {
            filter { Entidad::id eq id }
        }
}
```

### DELETE
```kotlin
suspend fun eliminar(id: String) {
    supabase.postgrest["tabla"]
        .delete {
            filter { Entidad::id eq id }
        }
}
```

### UPSERT
```kotlin
suspend fun guardar(entidad: Entidad): Entidad =
    supabase.postgrest["tabla"]
        .upsert(entidad) { select() }
        .decodeSingle<Entidad>()
```

### JOIN (con datos de tabla relacionada)
```kotlin
suspend fun obtenerConRelacion(): List<EntidadConRelacion> =
    supabase.postgrest["tabla"]
        .select(columns = Columns.raw("*, relacion:campo_id(*)"))
        .decodeList<EntidadConRelacion>()
```

### RPC (función PostgreSQL)
```kotlin
suspend fun ejecutarFuncion(param1: String, param2: Int): ResultType =
    supabase.postgrest
        .rpc(
            function = "nombre_funcion_sql",
            parameters = mapOf(
                "param1" to param1,
                "param2" to param2
            )
        )
        .decodeSingle<ResultType>()
```

## Agregar manejo de errores

Para operaciones críticas, envolver con try-catch:

```kotlin
suspend fun insertarConManejo(entidad: Entidad): Entidad? {
    return try {
        supabase.postgrest["tabla"]
            .insert(entidad) { select() }
            .decodeSingle<Entidad>()
    } catch (e: io.github.jan.supabase.exceptions.RestException) {
        println("Error Supabase [${e.statusCode}]: ${e.error}")
        null
    } catch (e: Exception) {
        println("Error de red: ${e.message}")
        null
    }
}
```

## Ejemplos reales del proyecto LexSign

### UserRepository (existente)
```kotlin
// data/repository/UserRepository.kt
import io.github.jan.supabase.postgrest.postgrest

class UserRepository {

    suspend fun registrarCliente(usuario: Usuario): Usuario =
        supabase.postgrest["users"]
            .insert(usuario) { select() }
            .decodeSingle<Usuario>()

    suspend fun autenticarCliente(email: String, passwordHash: String): Usuario? =
        supabase.postgrest["users"]
            .select {
                filter {
                    Usuario::email eq email
                    Usuario::password eq passwordHash
                    Usuario::tipo eq "cliente"
                }
            }
            .decodeSingleOrNull<Usuario>()

    suspend fun autenticarAbogado(email: String, tarjeta: String, passwordHash: String): Usuario? =
        supabase.postgrest["users"]
            .select {
                filter {
                    Usuario::email eq email
                    Usuario::tarjeta eq tarjeta
                    Usuario::password eq passwordHash
                    Usuario::tipo eq "abogado"
                }
            }
            .decodeSingleOrNull<Usuario>()

    suspend fun obtenerPorId(id: String): Usuario? =
        supabase.postgrest["users"]
            .select { filter { Usuario::id eq id } }
            .decodeSingleOrNull<Usuario>()
}
```

### ConsultaRepository (a crear)
```kotlin
// data/repository/ConsultaRepository.kt
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ConsultaRepository {

    suspend fun crear(consulta: Consulta): Consulta =
        supabase.postgrest["consultas"]
            .insert(consulta) { select() }
            .decodeSingle<Consulta>()

    suspend fun obtenerPorCliente(clienteId: String): List<Consulta> =
        supabase.postgrest["consultas"]
            .select {
                filter { Consulta::clienteId eq clienteId }
                order(column = "created_at", ascending = false)
            }
            .decodeList<Consulta>()

    suspend fun obtenerPorAbogado(abogadoId: String): List<Consulta> =
        supabase.postgrest["consultas"]
            .select {
                filter { Consulta::abogadoId eq abogadoId }
                order(column = "created_at", ascending = false)
            }
            .decodeList<Consulta>()

    suspend fun obtenerConDetalle(id: String): ConsultaDetalle? =
        supabase.postgrest["consultas"]
            .select(columns = Columns.raw("*, cliente:cliente_id(*), abogado:abogado_id(*)")) {
                filter { Consulta::id eq id }
            }
            .decodeSingleOrNull<ConsultaDetalle>()

    suspend fun actualizarEstado(id: String, estado: EstadoConsulta) {
        supabase.postgrest["consultas"]
            .update({ set("estado", estado.name.lowercase()) }) {
                filter { Consulta::id eq id }
            }
    }
}
```

## Naming conventions para métodos

| Operación | Nombre preferido |
|-----------|-----------------|
| Insert | `insertar()`, `crear()`, `registrar()` |
| Select all | `obtenerTodos()` |
| Select one | `obtenerPorId()`, `obtenerPorEmail()` |
| Select filtered | `buscarPor[Campo]()`, `filtrarPor[Campo]()` |
| Update | `actualizar()`, `actualizar[Campo]()` |
| Delete | `eliminar()`, `eliminarPorId()` |
| Upsert | `guardar()` |
| RPC | nombre descriptivo de la función |

## Reglas de retorno

| Caso | Retorno correcto |
|------|-----------------|
| Insert que devuelve el registro | `Entidad` |
| Select de lista | `List<Entidad>` |
| Select que puede no existir | `Entidad?` |
| Update / Delete (sin retorno) | `Unit` (omitir tipo) |
| Upsert que devuelve el registro | `Entidad` |
| Con manejo de error | `Entidad?` o `List<Entidad>` (vacía en error) |

## Imports necesarios

```kotlin
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns  // solo para joins
import io.github.jan.supabase.exceptions.RestException // solo para try-catch
```

## Verificación después de agregar método

```powershell
.\gradlew.bat :composeApp:build
```

Errores comunes:
- `unresolved reference: postgrest` → verificar import
- `unresolved reference: eq` / `ilike` → import de filtros supabase-kt
- `Type mismatch` → verificar tipo de retorno (`decodeSingle` vs `decodeList`)
