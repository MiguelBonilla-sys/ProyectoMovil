# GitHub Copilot Instructions — LexSign

## Descripción del proyecto
LexSign es una app móvil **Kotlin Multiplatform (KMP)** que conecta clientes con abogados en Colombia. Permite gestionar consultas legales, firmar documentos digitalmente y hacer seguimiento de casos. Backend: **Supabase (PostgreSQL)**.

---

## Stack tecnológico
| Capa | Tecnología |
|---|---|
| UI | Jetpack Compose (Multiplatform) |
| ViewModel | `androidx.lifecycle.ViewModel` + `StateFlow` |
| Base de datos | Supabase Cloud (PostgreSQL) via `supabase-kt` v3.3.0 |
| Auth | Custom PBKDF2 hashing (sin Supabase Auth) |
| Networking | Ktor Client (Android: `ktor-client-android`) |
| Serialización | `kotlinx-serialization-json` |
| Navegación | Jetpack Navigation Compose |
| Plataformas | Android (primary), iOS (configured) |

---

## Arquitectura en capas
```
Presentation (Screens) → ViewModel → Repository → Supabase (postgrest-kt)
```

### Patrones obligatorios
- **Models**: `@Serializable data class` con `@SerialName` para cada campo de Supabase
- **Repositories**: `suspend fun` que llaman directamente a `supabase.from("tabla")`
- **ViewModels**: `StateFlow<UiState>` + `viewModelScope.launch` para corrutinas
- **Estado UI**: un `data class UiState` por ViewModel con `isLoading`, `error`, `data`
- **Session**: `SessionManager.currentUser` para obtener el usuario autenticado

---

## Convenciones de código

### Colores del tema (dark UI)
```kotlin
Color(0xFF0D0D1A)  // fondo pantallas
Color(0xFF1E1E2E)  // cards / surface
Color(0xFF3949AB)  // primary (azul índigo)
Color(0xFF00BFA5)  // secondary (teal)
Color(0xFFFFA726)  // warning (naranja)
Color(0xFFCF6679)  // error (rojo)
Color.White        // textos principales
Color.White.copy(alpha = 0.7f)  // textos secundarios
```

### Estructura de archivos
```
composeApp/src/commonMain/kotlin/com/example/proyecto/
├── data/
│   ├── model/          # @Serializable data classes (User, Consulta, Documento, Notificacion)
│   ├── remote/         # SupabaseClientProvider.kt, supabase instance
│   ├── repository/     # AuthRepository, ConsultaRepository, DocumentoRepository, etc.
│   ├── session/        # SessionManager.kt (currentUser: User?)
│   └── util/           # SecurityUtils (expect/actual PBKDF2)
├── navigation/
│   └── NavGraph.kt     # Routes object + AppNavGraph composable
└── ui/
    ├── screens/        # *Screen.kt composables
    ├── theme/          # Color.kt, Theme.kt, Type.kt
    └── viewmodel/      # *ViewModel.kt con UiState
```

### Cómo hacer una query Supabase
```kotlin
// SELECT con filtro
supabase.from("tabla")
    .select { filter { eq("columna", valor) } }
    .decodeList<Model>()

// SELECT paginado (siempre usar PAGE_SIZE = 20)
supabase.from("tabla")
    .select {
        filter { eq("columna", valor) }
        order("created_at", Order.DESCENDING)
        range(offset.toLong(), (offset + limit - 1).toLong())
    }
    .decodeList<Model>()

// INSERT y retornar el objeto creado
supabase.from("tabla")
    .insert(objeto) { select() }
    .decodeSingle<Model>()

// UPDATE
supabase.from("tabla")
    .update({ set("campo", valor) }) { filter { eq("id", id) } }

// DELETE
supabase.from("tabla")
    .delete { filter { eq("id", id) } }
```

### Patrón ViewModel completo
```kotlin
data class MiUiState(
    val datos: List<Model> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MiViewModel(
    private val repository: MiRepository = MiRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MiUiState())
    val uiState: StateFlow<MiUiState> = _uiState.asStateFlow()

    init { cargarDatos() }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val lista = repository.obtenerTodos()
                _uiState.value = _uiState.value.copy(datos = lista, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
```

### Patrón Screen
```kotlin
@Composable
fun MiScreen(viewModel: MiViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Color(0xFF0D0D1A)) { padding ->
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Color(0xFF3949AB))
            uiState.error != null -> Text("Error: ${uiState.error}", color = Color(0xFFCF6679))
            else -> LazyColumn { items(uiState.datos) { item -> MiCard(item) } }
        }
    }
}
```

---

## Modelos del dominio

### User
```kotlin
@Serializable data class User(
    val id: String? = null,               // UUID Supabase
    val nombre: String,
    val email: String,
    val password: String,                 // hash PBKDF2
    val tipo: String,                     // "cliente" | "abogado" | "administrador"
    val tarjeta: String? = null,
    val especialidad: String? = null,
    val experiencia: Int? = null,
    val descripcion: String? = null,
    val telefono: String? = null,
    val calificacionPromedio: Double? = null,
    val fotoUrl: String? = null
)
// val esAdmin: Boolean get() = tipo == "administrador"
// val initials: String = primeras letras del nombre
```

### Consulta
```kotlin
@Serializable data class Consulta(
    val id: String? = null,
    val clienteId: String,
    val estado: EstadoConsulta = EstadoConsulta.ABIERTA,  // ABIERTA | EN_CURSO | CERRADA
    val areaPractica: String,
    val descripcion: String,
    val createdAt: String? = null
)
```

### Documento
```kotlin
@Serializable data class Documento(
    val id: String? = null,
    val consultaId: String? = null,
    val nombre: String,
    val url: String,
    val tipo: String,
    val esPlantilla: Boolean = false,
    val subidoPor: String,
    val estadoFirma: EstadoFirma = EstadoFirma.PENDIENTE  // PENDIENTE | FIRMADO | RECHAZADO
)
```

---

## Tablas Supabase
| Tabla | Descripción |
|---|---|
| `users` | Clientes, abogados y administradores |
| `consultas` | Consultas legales |
| `consulta_abogados` | Relación N:M consulta ↔ abogado |
| `documentos` | Documentos y plantillas |
| `notificaciones` | Notificaciones en tiempo real |

Buckets de Storage: `documentos` (privado), `plantillas` (público)

---

## Navegación
```
Routes.WELCOME → Routes.LOGIN_CLIENTE / LOGIN_ABOGADO → Routes.MAIN
Routes.MAIN → MainScreen (tabs: Inicio / Abogados / Documentos / Consultas / Perfil)
Routes.CREATE_CONSULTATION?abogadoId={}&abogadoNombre={} → CreateConsultationScreen
```

---

## Reglas de negocio importantes
- **Autenticación**: custom con PBKDF2 (NO usa Supabase Auth). `SessionManager.currentUser` es la sesión activa.
- **Firma de documentos**: Solo puede firmar quien NO subió el documento y el estado es PENDIENTE.
- **Eliminar consulta**: Solo se pueden eliminar consultas con estado ABIERTA.
- **Eliminar documento**: Solo puede eliminar quien lo subió (`subidoPor == currentUser.id`).
- **Paginación**: Siempre usar `PAGE_SIZE = 20` con `.range(offset, offset+limit-1)`.
- **Roles**: `tipo == "administrador"` habilita tab de Reportes en MainScreen.

---

## Variables de entorno
Definidas en `.env` (NO versionado), inyectadas como `BuildConfig` via `composeApp/build.gradle.kts`:
- `SUPABASE_URL` — URL del proyecto Supabase
- `SUPABASE_ANON_KEY` — clave pública anon key

---

## Comandos útiles
```bash
# Compilar
./gradlew :composeApp:compileDebugKotlin

# Build completo
./gradlew :composeApp:assembleDebug

# Supabase: aplicar migraciones
npx supabase login
npx supabase link --project-ref fyyqhtykapzdvugkluty
npx supabase db push

# Supabase: ver diferencias con BD remota
npx supabase db diff
```
