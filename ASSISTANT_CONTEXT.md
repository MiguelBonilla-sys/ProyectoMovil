# Proyecto LexSign - Kotlin Multiplatform

## Visión General
**LexSign** es una aplicación Kotlin Multiplatform (KMP) que conecta clientes con abogados para servicios legales. Implementa una arquitectura offline-first con sincronización en la nube usando SQLDelight (local) y Supabase (remoto).

## Arquitectura de Datos (Offline-First)

### Stack Tecnológico
- **Local:** SQLDelight 2.0.2 (SQLite) para acceso rápido y offline
- **Remoto:** Supabase 3.3.0 (PostgreSQL) para sincronización y backup en la nube
- **HTTP Client:** Ktor 3.1.1 para comunicación de red
- **Serialización:** Kotlinx Serialization para JSON

### Flujo de Datos

```
┌─────────────────────────────────────────────────────────┐
│                    Aplicación LexSign                    │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  UI (Compose) ─→ ViewModel ─→ Repository ─→ Database   │
│                                     ↓                     │
│                              ┌──────┴──────┐            │
│                              │   SQLite    │             │
│                              │   (Local)   │             │
│                              └──────┬──────┘            │
│                                     ↓                     │
│                           Sync (cuando online)           │
│                                     ↓                     │
│                              ┌──────┴──────┐            │
│                              │  Supabase   │             │
│                              │ (PostgreSQL) │             │
│                              └─────────────┘            │
│                                                          │
└─────────────────────────────────────────────────────────┘

Principios:
1. Todas las lecturas vienen de SQLite (rápido, offline)
2. Todas las escrituras van primero a SQLite (offline-capable)
3. Escrituras se sincronizan a Supabase en background (cuando hay internet)
4. Conflictos se marcan con syncStatus = 'conflict'
```

## Estructura del Proyecto

### composeApp/
Contiene el código compartido que se compila para múltiples plataformas:
- `commonMain/kotlin`: Código común para todas las plataformas (Android, iOS)
  - `data/`: Capa de datos
    - `model/`: Data classes (User, etc.)
    - `repository/`: Repositorios con lógica de negocio
    - `remote/`: Cliente Supabase
    - `storage/`: FileStorage (expect/actual) [DEPRECATED - usar SQLDelight]
    - `session/`: SessionManager para manejo de sesión
    - `AppContainer.kt`: DI container con instancias singleton
  - `database/`: SQLDelight database y drivers (expect/actual)
  - `ui/`: Interfaz de usuario
    - `screens/`: Pantallas de la app
    - `theme/`: Tema y estilos
    - `viewmodel/`: ViewModels para gestión de estado
  - `navigation/`: Navegación de la app
- `iosMain/kotlin`: Código específico para iOS
  - `database/DatabaseDriverFactory.kt`: NativeSqliteDriver
- `androidMain/kotlin`: Código específico para Android
  - `MainActivity.kt`: Inicializa AppContainer
  - `database/DatabaseDriverFactory.kt`: AndroidSqliteDriver
- `commonMain/sqldelight/`: Esquemas SQL
  - `com/example/proyecto/database/User.sq`: Esquema de usuarios

### iosApp/
Contiene la aplicación nativa de iOS que sirve como punto de entrada.

## Componentes Clave

### 1. Database Layer

#### DatabaseDriverFactory (expect/actual)
```kotlin
// commonMain
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(...)
}

// iosMain
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(...)
}
```

#### User.sq (SQL Schema)
- **Tabla:** `User`
- **Columnas:**
  - `id`: INTEGER PRIMARY KEY AUTOINCREMENT
  - `nombre`: TEXT NOT NULL
  - `email`: TEXT NOT NULL UNIQUE
  - `password`: TEXT NOT NULL
  - `tipo`: TEXT NOT NULL ('cliente' | 'abogado')
  - `tarjeta`: TEXT NOT NULL (tarjeta profesional para abogados)
  - `supabaseId`: TEXT UNIQUE (ID del usuario en Supabase)
  - `createdAt`: INTEGER (timestamp en milisegundos)
  - `updatedAt`: INTEGER (timestamp en milisegundos)
  - `syncStatus`: TEXT ('synced' | 'pending' | 'conflict')

- **Queries:**
  - Autenticación: `authenticateCliente`, `authenticateAbogado`
  - CRUD: `insert`, `update`, `deleteById`, `selectAll`, `selectByEmail`
  - Sincronización: `selectPendingSync`, `updateSyncStatus`

### 2. Repository Layer

#### UserRepository
```kotlin
class UserRepository(private val database: LexSignDatabase) {
    // Autenticación
    suspend fun authenticateCliente(email: String, password: String): User?
    suspend fun authenticateAbogado(email: String, tarjeta: String, password: String): User?
    
    // Registro
    suspend fun registerCliente(nombre: String, email: String, password: String): Result<User>
    
    // CRUD
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserByEmail(email: String): User?
    
    // Sincronización
    suspend fun syncPendingToSupabase()
    suspend fun pullFromSupabase()
    
    // Inicialización
    suspend fun init(seedUsers: List<User>)
}
```

### 3. Supabase Integration

#### SupabaseClientProvider
```kotlin
object SupabaseClientProvider {
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL_HERE"
    private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY_HERE"
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
    
    fun isConfigured(): Boolean
}
```

**TODO:** Reemplazar placeholders con credenciales reales de Supabase.

**Configuración de Supabase requerida:**
1. Crear proyecto en https://supabase.com
2. Crear tabla `users` con columnas:
   - id (uuid, primary key, default: gen_random_uuid())
   - nombre (text, not null)
   - email (text, not null, unique)
   - password (text, not null)
   - tipo (text, not null)
   - tarjeta (text, not null, default: '')
   - created_at (timestamptz, not null, default: now())
   - updated_at (timestamptz, not null, default: now())
3. Configurar políticas RLS o desactivar temporalmente para desarrollo
4. Copiar URL y anon key desde Settings > API

### 4. ViewModel Layer

#### AuthViewModel
```kotlin
class AuthViewModel(
    private val repository: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    var isLoading: Boolean
    var error: String?
    
    fun loginCliente(email: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit)
    fun loginAbogado(email: String, tarjeta: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit)
    fun registerCliente(nombre: String, email: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit)
}
```

### 5. Dependency Injection

#### AppContainer
```kotlin
object AppContainer {
    fun initialize(driverFactory: DatabaseDriverFactory)
    val userRepository: UserRepository
    val database: LexSignDatabase
}
```

**Inicialización en MainActivity:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    appContext = applicationContext
    AppContainer.initialize(DatabaseDriverFactory(applicationContext))
    // ...
}
```

### 6. UI Screens

- **WelcomeScreen:** Pantalla inicial con opciones para clientes y abogados
- **LoginClienteScreen:** Login de clientes (email + password)
- **LoginAbogadoScreen:** Login de abogados (email + tarjeta profesional + password)
- **RegisterClienteScreen:** Registro de nuevos clientes
- **MainScreen:** Pantalla principal después del login

**Todas las pantallas ahora usan AuthViewModel** para operaciones de autenticación con:
- Loading states (CircularProgressIndicator)
- Error handling
- Callbacks para navegación

## Archivos de Configuración Importantes

### build.gradle.kts (root)
Define plugins:
- Kotlin Multiplatform
- Compose Multiplatform
- Android Application
- SQLDelight
- Kotlin Serialization

### composeApp/build.gradle.kts
Configura:
- Targets (Android, iOS)
- Dependencies (SQLDelight, Supabase, Ktor, Compose)
- SQLDelight plugin con `LexSignDatabase`

### gradle/libs.versions.toml
Versiones de dependencias:
- sqldelight = "2.0.2"
- supabase = "3.3.0"
- ktor = "3.1.1"
- kotlin = "2.3.0"
- compose = "1.8.0"

## Instrucciones de Build y Ejecución

### Android
```bash
# Windows
.\gradlew.bat :composeApp:assembleDebug

# macOS/Linux
./gradlew :composeApp:assembleDebug
```

### iOS
Abrir proyecto en Xcode: `iosApp/iosApp.xcodeproj` y ejecutar.

## Datos de Prueba (Seed Data)

Los usuarios iniciales se cargan desde `composeApp/src/commonMain/composeResources/files/users.json`:

**Clientes:**
- cliente1@example.com / password123
- cliente2@example.com / password123

**Abogados:**
- abogado1@example.com / TARJ-001 / password123
- abogado2@example.com / TARJ-002 / password123
- abogado3@example.com / TARJ-003 / password123

## Próximos Pasos

1. **Configurar Supabase:**
   - [ ] Crear proyecto
   - [ ] Crear tabla `users`
   - [ ] Configurar RLS
   - [ ] Actualizar credenciales en `SupabaseClientProvider.kt`

2. **Testing:**
   - [ ] Crear `UserRepositoryTest.kt`
   - [ ] Crear `AuthViewModelTest.kt`
   - [ ] Probar sincronización offline/online

3. **Mejoras:**
   - [ ] Implementar resolución de conflictos
   - [ ] Agregar refresh tokens para Supabase Auth
   - [ ] Implementar sincronización periódica en background
   - [ ] Agregar logs de sincronización
   - [ ] Migrar FileStorage a usar solo SQLDelight

## Tecnologías Utilizadas
- **Kotlin Multiplatform** (código compartido entre plataformas)
- **Jetpack Compose Multiplatform** (UI declarativa)
- **SQLDelight** (type-safe SQL)
- **Supabase** (backend as a service)
- **Ktor** (HTTP client)
- **Kotlinx Serialization** (JSON serialización)
- **Gradle with Version Catalogs** (gestión de dependencias)

## Notas Importantes
1. El código compartido en `commonMain` puede acceder a APIs específicas de plataforma mediante **expect/actual**.
2. SQLDelight genera código Kotlin type-safe desde archivos `.sq`.
3. El repositorio sigue el patrón **offline-first**: lee siempre de local, escribe local primero, sincroniza después.
4. SessionManager mantiene el estado de sesión en memoria (sin persistencia entre reinicios).
5. La sincronización a Supabase solo ocurre si `SupabaseClientProvider.isConfigured()` es `true`.
6. Todos los timestamps están en milisegundos desde epoch (Unix timestamp * 1000).