# Deployment - LexSign

## Requisitos Previos

- Node.js 18+
- Java 17+ (JDK)
- Android Studio / Xcode
- Cuenta Supabase (producción)
- Cuenta CAMERFIRMA (producción)

## Variables de Entorno

### .env.production
```
SUPABASE_URL=https://fyyqhtykapzdvugkluty.supabase.co
SUPABASE_ANON_KEY=<anon_key_produccion>
SUPABASE_DB_PASSWORD=<db_password_produccion>
CAMERFIRMA_URL=https://api.camerfirma.com
CAMERFIRMA_API_KEY=<api_key_produccion>
CAMERFIRMA_SANDBOX=false
```

## Build Android

```bash
cd composeApp
./gradlew :composeApp:assembleRelease
```

Output: `composeApp/build/outputs/apk/release/app-release.apk`

## Build iOS

```bash
cd iosApp
xcodebuild -workspace iosApp.xcworkspace \
    -scheme iosApp \
    -configuration Release \
    -archivePath build/iosApp.xcarchive \
    archive
```

## Supabase Producción

1. Crear nuevo proyecto en [supabase.com](https://supabase.com)
2. Link: `supabase link --project-ref <project_id>`
3. Push migraciones: `supabase db push`
4. Configurar Auth (Email/Password)
5. Crear bucket `documents` con storage rules:
   ```json
   {
     "public": false,
     "allowed": ["application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]
   }
   ```

## Despliegue Firebase (Opcional)

Para push notifications:
```bash
cd functions
firebase deploy --only functions
```

## Post-Deploy

1. ✅ Verificar RLS policies
2. ✅ Configurar backups automáticos (Supabase Dashboard)
3. ✅ Habilitar 2FA para admin
4. ✅ Configurar dominios personalizados
5. ✅ Probar flujo completo de firma
6. ✅ Monitoreo (UptimeRobot, etc.)

## Usuarios de Prueba

| Email | Contraseña | Rol |
|-------|------------|-----|
| admin@lexsign.com | Admin123! | Administrador |
| cliente@test.com | Cliente123! | Cliente |
| abogado1@test.com | Abogado123! | Abogado |

## Troubleshooting

### Build falla
```bash
./gradlew clean
./gradlew :composeApp:assembleRelease --stacktrace
```

### Supabase Connection Error
- Verificar .env configurado
- Verificar ANON_KEY tiene permisos
- Revisar logs en Supabase Dashboard

### Firma Electrónica no funciona
- Verificar credenciales CAMERFIRMA
- Probar en sandbox primero
- Revisar webhooks configurados
