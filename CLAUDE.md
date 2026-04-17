# LexSign — Contexto del proyecto para Claude Code

## Stack
- **KMP (Kotlin Multiplatform):** Android + iOS
- **UI:** Jetpack Compose Multiplatform
- **Backend:** Supabase PostgreSQL (cloud-first, sin BD local)
- **Auth:** Custom (tabla `users` propia, NO Supabase Auth)
- **Hashing:** PBKDF2WithHmacSHA256 — `SecurityUtils` (`expect`/`actual` por plataforma)
- **Cliente Supabase:** `io.github.jan.supabase` (supabase-kt 3.x)
- **Networking:** Ktor 3.x

## Arquitectura de carpetas
```
composeApp/src/commonMain/kotlin/com/example/proyecto/
├── data/
│   ├── model/          # Data classes @Serializable con @SerialName(snake_case)
│   ├── remote/         # SupabaseClient singleton (anon key desde BuildConfig)
│   ├── repository/     # Repositorios: suspend fun → supabase.from(tabla)
│   ├── session/        # SessionManager (usuario en memoria + SharedPreferences)
│   └── util/           # SecurityUtils (expect/actual)
├── navigation/
│   └── NavGraph.kt     # AppNavGraph + objeto Routes
└── ui/
    ├── screens/        # Composables de pantalla
    └── viewmodel/      # ViewModels con StateFlow
```

## Autenticación — IMPORTANTE
El proyecto usa **autenticación custom**, no Supabase Auth:
- Los usuarios viven en la tabla `public.users` (no en `auth.users`)
- Login verifica contraseña con `SecurityUtils.verifyPassword(plain, hash)`
- Hash formato: `Base64(salt):Base64(PBKDF2)` — 10 000 iteraciones, SHA-256, 256 bits
- `autenticarCliente()` acepta `tipo = "cliente"` **o** `tipo = "administrador"`
- `autenticarAbogado()` requiere email + tarjeta profesional + contraseña

## RLS — Estado actual
Las siguientes tablas tienen RLS **desactivado** (necesario porque se usa anon key con auth custom):
- `consultas`, `consulta_abogados`, `documentos`, `notificaciones`

La tabla `users` permite INSERT y UPDATE con la anon key (políticas permisivas).

## Usuarios de prueba
| Email | Contraseña | Tipo | Pantalla de login | Tarjeta |
|---|---|---|---|---|
| admin@lexsign.com | Admin123! | administrador | "Ingresar al Sistema" | — |
| cliente@test.com | Cliente123! | cliente | "Ingresar al Sistema" | — |
| abogado1@test.com | Abogado123! | abogado | "Soy Abogado" | AB-12345 |
| abogado2@test.com | Abogado456! | abogado | "Soy Abogado" | AB-67890 |

## Rutas de navegación (Routes.kt)
```
welcome → login_cliente / login_abogado → main
main (tabs): Home | Abogados | Documentos | Consultas | Perfil | Reportes(admin)
main → create_consultation?abogadoId=&abogadoNombre=
main → security
main → notifications
main → help_center
main → terms
```

## Patrones clave

### Repository
```kotlin
// NO usar decodeSingle() después de UPDATE — devuelve lista vacía con anon key
// Usar decodeSingleOrNull() o construir objeto localmente
supabase.from("tabla").update({ set("col", val) }) { filter { eq("id", id) } }
// Luego construir el objeto actualizado manualmente
```

### ViewModel
```kotlin
class MiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MiUiState())
    val uiState: StateFlow<MiUiState> = _uiState.asStateFlow()
    // Coroutines en viewModelScope.launch {}
}
```

### Modelo de datos
```kotlin
@Serializable
data class MiModelo(
    @SerialName("id") val id: String? = null,       // null = auto-generado por DB
    @SerialName("campo_snake") val campoKt: String,
    @SerialName("created_at") val createdAt: String? = null
)
```

## Pantallas implementadas
- **WelcomeScreen** — entrada, botones login cliente/abogado
- **LoginClienteScreen / LoginAbogadoScreen** — formularios de login
- **RegisterClienteScreen / RegisterAbogadoScreen** — registro
- **MainScreen** — bottom nav con tabs
- **HomeScreen** — dashboard
- **LawyersScreen** — lista abogados + dialog de detalle + "Solicitar consulta"
- **ConsultationsScreen** — lista consultas + dialog crear consulta (FAB)
- **CreateConsultationScreen** — formulario completo nueva consulta
- **DocumentsScreen** — lista docs + tabs Mis Docs/Plantillas + FAB con menú (Subir/Plantilla)
- **ProfileScreen** — perfil de usuario + menú de opciones
- **SecurityScreen** — cambiar contraseña
- **NotificationsScreen** — lista notificaciones con Realtime
- **HelpCenterScreen** — FAQ expandibles
- **TermsScreen** — términos y condiciones
- **ReportesScreen** — métricas admin (solo tipo="administrador")

## Supabase — tablas principales
- `users` — usuarios (cliente, abogado, administrador)
- `consultas` — consultas legales (`cliente_id`, `area_practica`, `descripcion`, `estado`)
- `consulta_abogados` — relación consulta ↔ abogado
- `documentos` — documentos y plantillas (`es_plantilla`, `url`, `estado_firma`)
- `notificaciones` — notificaciones push con Realtime
- Storage buckets: `documentos` (privado), `plantillas` (público)

## Comandos útiles
```bash
# Build Android debug
.\gradlew.bat :composeApp:assembleDebug

# Seed / verificar usuarios en Supabase
python scripts/seed_users.py
```

## Reglas para Claude Code
- Responder siempre en **español**
- Nunca commitear `.env` (tiene las credenciales reales)
- Antes de editar, leer el archivo completo
- No agregar comentarios obvios — solo documentar el WHY cuando no es evidente
- Para UPDATE en Supabase con anon key: no usar `.select()` + `decodeSingle()` — devuelve vacío
