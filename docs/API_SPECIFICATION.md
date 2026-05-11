# API Specification - LexSign

## Base URL
```
https://fyyqhtykapzdvugkluty.supabase.co
```

## Authentication
Todas las peticiones requieren header `Authorization: Bearer <token>` con el token de Supabase.

---

## Tablas

### users
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Primary key |
| nombre | TEXT | Nombre completo |
| email | TEXT | Email único |
| password | TEXT | Hash PBKDF2 |
| tipo | TEXT | cliente/abogado/administrador |
| rol | TEXT | Rol del usuario |
| tarjeta | TEXT | Tarjeta profesional (abogados) |
| especialidad | TEXT | Especialidad legal |
| created_at | TIMESTAMPTZ | Fecha creación |

### consultas
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Primary key |
| cliente_id | UUID | FK a users |
| estado | TEXT | abierta/en_curso/cerrada |
| area_practica | TEXT | Área legal |
| descripcion | TEXT | Descripción consulta |
| created_at | TIMESTAMPTZ | Fecha creación |

### documentos
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Primary key |
| consulta_id | UUID | FK a consultas |
| nombre | TEXT | Nombre archivo |
| url | TEXT | URL storage |
| tipo | TEXT | MIME type |
| es_plantilla | BOOLEAN | Si es plantilla |
| estado_firma | TEXT | pendiente/firmado/rechazado |
| proceso_firma_id | TEXT | ID CAMERFIRMA |
| sello_tiempo_verificado | TIMESTAMPTZ | Sello de tiempo |
| tipo_firma | TEXT | SIMPLE/AVANZADA |
| subido_por | UUID | FK a users |
| created_at | TIMESTAMPTZ | Fecha creación |

### notificaciones
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Primary key |
| usuario_id | UUID | FK a users |
| tipo | TEXT | consulta_nueva/documento_firmado/estado_cambio/sistema |
| titulo | TEXT | Título |
| mensaje | TEXT | Mensaje |
| leida | BOOLEAN | Si fue leída |
| created_at | TIMESTAMPTZ | Fecha creación |

### audit_logs
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Primary key |
| usuario_id | UUID | FK a users |
| tabla_afectada | TEXT | Tabla modificada |
| accion | TEXT | INSERT/UPDATE/DELETE |
| registro_id | UUID | ID registro modificado |
| datos_anteriores | JSONB | Datos antes |
| datos_nuevos | JSONB | Datos después |
| ip_origen | INET | IP del cliente |
| created_at | TIMESTAMPTZ | Fecha |

---

## Endpoints Principales

### Auth
- `POST /auth/v1/signup` - Registro
- `POST /auth/v1/token?grant_type=password` - Login
- `POST /auth/v1/logout` - Logout

### Storage
- `POST /storage/v1/object/documents/<path>` - Subir archivo
- `GET /storage/v1/object/documents/<path>` - Descargar archivo
- `DELETE /storage/v1/object/documents/<path>` - Eliminar archivo

---

## RLS Policies

### users
- SELECT: Público
- INSERT: Público  
- UPDATE: Solo propio (`auth.uid() = id`)

### consultas
- SELECT: Cliente dueño o abogados asignados
- INSERT: Solo cliente (`cliente_id = auth.uid()`)
- UPDATE: Cliente dueño o abogados asignados

### documentos
- SELECT: Dueño, plantillas públicas, o participantes de consulta
- INSERT: Solo sube quien (`subido_por = auth.uid()`)
- UPDATE: Solo dueño (`subido_por = auth.uid()`)

### notificaciones
- SELECT: Solo propias (`usuario_id = auth.uid()`)
- INSERT: Público
- UPDATE: Solo propias (`usuario_id = auth.uid()`)

### audit_logs
- SELECT: Solo administradores
- INSERT: Público (triggers internos)
