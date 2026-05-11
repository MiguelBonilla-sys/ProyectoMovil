# Arquitectura LexSign

## Stack Tecnológico
- **Frontend**: Kotlin Multiplatform (Compose)
- **Backend**: Supabase (PostgreSQL + Auth + Storage + Realtime)
- **Firma Electrónica**: CAMERFIRMA API
- **Build**: Gradle + Kotlin DSL

## Capas de la Aplicación

```
┌─────────────────────────────────────────┐
│              UI (Compose)              │
│  Screens, ViewModels, Navigation         │
├─────────────────────────────────────────┤
│           Data Layer                    │
│  Repositories, Models, Remote API        │
├─────────────────────────────────────────┤
│         Session/Storage                 │
│  SessionManager, Supabase Client        │
└─────────────────────────────────────────┘
```

## Estructura de Paquetes

```
composeApp/src/commonMain/kotlin/com/example/proyecto/
├── data/
│   ├── model/          # Data classes (Documento, Consulta, User, etc.)
│   ├── remote/          # Supabase client setup
│   ├── repository/      # Repository implementations
│   ├── session/         # Session management
│   └── util/            # Utilities (ValidationUtils, SecurityUtils)
├── ui/
│   ├── screens/         # Compose UI screens
│   ├── theme/           # Material theme
│   └── viewmodel/       # ViewModels (MVVM pattern)
└── navigation/          # Navigation graph
```

## Patrón MVVM

```kotlin
// ViewModel: Gestiona estado y lógica
class DocumentoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentoUiState())
    val uiState: StateFlow<DocumentoUiState> = _uiState.asStateFlow()
    
    fun cargarDocumentos() { ... }
}

// Screen: Observa estado y renderiza UI
@Composable
fun DocumentosScreen(viewModel: DocumentoViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // UI...
}
```

## Flujo de Datos

1. **User Action** → Screen llama método en ViewModel
2. **ViewModel** → Ejecuta lógica de negocio
3. **Repository** → Llama Supabase (POSTGREST)
4. **Supabase** → Procesa request, aplica RLS
5. **Response** → ViewModel actualiza UI State
6. **Screen** → Recompose con nuevos datos

## Supabase Integration

```kotlin
// Singleton client
object SupabaseClientProvider {
    val client: SupabaseClient by lazy {
        createClient(
            projectUrl = "https://xxx.supabase.co",
            clientKey = "eyJhbGc..."
        )
    }
}

// Repository usa client
class DocumentoRepository {
    private val supabase = SupabaseClientProvider.client
    private val postgrest = supabase.postgrest
    
    suspend fun obtenerPorUsuario(userId: String): List<Documento> {
        return postgrest.from("documentos")
            .select { filter { eq("subido_por", userId) } }
            .decodeList<Documento>()
    }
}
```

## Autenticación

```kotlin
// Login
suspend fun login(email: String, password: String): Result<User> {
    val session = supabase.auth.signInWithPassword(email, password)
    SessionManager.currentUser = session.user
    return Result.success(session.user)
}
```

## Realtime

```kotlin
// Suscripción a notificaciones
val channel = supabase.realtime.channel("notificaciones-$userId")
channel.postgresChangeFlow<PostgresAction.Insert> {
    table = "notificaciones"
}.onEach { action ->
    // Actualizar UI en tiempo real
}.launchIn(viewModelScope)
```

## Storage (Documentos)

- Bucket: `documents`
- Path: `documents/<userId>/<documentId>/<filename>`
- URLs firmadas para acceso temporal

## Modelo de Firma Electrónica

```
Documento → ProcesoFirma → FirmasRegistros
    │              │              │
    │              ▼              ▼
    │         estados:     - usuario_id
    │         iniciado      - timestamp
    │         en_progreso   - ip
    │         completo      - certificado
    │         expirado      - firma_digital
    │
    ▼
 CAMERFIRMA API
```

## Decisiones Técnicas

| Decisión | Justificación |
|----------|---------------|
| KMP vs Native | Código compartido Android/iOS |
| Supabase vs Firebase | PostgreSQL + mejor RLS |
| PBKDF2 | Seguridad passwords (no BCrypt) |
| Realtime polling | Actualizaciones instantáneas |
| RLS policies | Seguridad a nivel de BD |
