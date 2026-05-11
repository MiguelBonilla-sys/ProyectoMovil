# Manual de Usuario - LexSign

## 1. Introducción

**LexSign** es una aplicación móvil de firma electrónica和法律咨询服务 que permite a clientes y abogados gestionar consultas legales y documentos con firma electrónica válida legalmente en Colombia (Ley 527/1999).

---

## 2. Registro e Inicio de Sesión

### Cliente
1. Abre la app y selecciona **"Registrarse como Cliente"**
2. Ingresa tu nombre completo
3. Ingresa tu email y crea una contraseña segura:
   - Mínimo 8 caracteres
   - Al menos 1 mayúscula
   - Al menos 1 número
4. Selecciona las áreas de práctica de interés
5. ¡Listo! Ya puedes crear consultas

### Abogado
1. Selecciona **"Registrarse como Abogado"**
2. Ingresa tu nombre completo
3. Ingresa tu tarjeta profesional (ej: AB-12345)
4. Selecciona tu especialidad
5. Agrega tu experiencia (años)
6. Describe tu perfil profesional
7. ¡Listo! Los clientes podrán encontrarte

---

## 3. Guía Rápida

### Para Clientes

#### Crear una Consulta
1. Ve a **"Mis Consultas"** en el drawer
2. Toca el botón **"+"**
3. Selecciona el **área legal** (Civil, Laboral, Familiar, etc.)
4. Describe tu consulta en detalle
5. Recibirás notificaciones cuando un abogado responda

#### Subir Documentos
1. Ve a **"Documentos"**
2. Toca **"+"**
3. Selecciona **"Subir archivo"** o **"Crear con plantilla"**
4. Asigna un nombre y tipo al documento
5. El documento estará disponible para compartir

#### Firmar Documentos
1. Ve a **"Documentos"**
2. Abre un documento pendiente de firma
3. Toca **"Firmar documento"**
4. Serás redirigido al portal de firma CAMERFIRMA
5. Completa la firma con tu certificado digital
6. ¡Listo! El documento queda firmado con sello de tiempo

### Para Abogados

#### Ver Consultas
1. Ve a **"Consultas"** en el drawer
2. Verás todas las consultas abiertas de clientes
3. Filtra por área, estado o búsqueda

#### Asignarte a una Consulta
1. Abre una consulta abierta
2. Toca **"Asignarme"**
3. El cliente recibirá una notificación
4. Puedes comenzar a interactuar con el cliente

#### Enviar Documentos
1. Ve a **"Documentos"**
2. Sube documentos relacionados con la consulta
3. El cliente puede firmarlos electrónicamente

### Para Administradores

#### Ver Auditoría
1. Ve a tu **Perfil**
2. Selecciona **"Auditoría del Sistema"**
3. Ve todos los cambios realizados en la plataforma
4. Exporta logs como CSV para análisis

---

## 4. Funcionalidades Clave

### Notificaciones en Tiempo Real
Las notificaciones aparecen instantáneamente sin necesidad de recargar la app.

### Búsqueda Avanzada
- **Por texto**: Busca en nombres y descripciones
- **Por estado**: Filtra por estado (abierta, en curso, cerrada)
- **Por área**: Filtra por área legal
- **Por fecha**: Rangos de fechas

### Firma Electrónica
- Sello de tiempo RFC 3161
- Certificado digital CAMERFIRMA
- No repudio
- Cadena de custodia

---

## 5. Preguntas Frecuentes

**¿La firma es legalmente válida?**
Sí. LexSign usa CAMERFIRMA, una entidad certificadora autorizada en Colombia (Ley 527/1999).

**¿Cuánto dura el proceso de firma?**
El sello de tiempo tiene validez de 30 días. Una vez firmado, el documento es permanente.

**¿Puedo usar la app en iOS?**
Sí. La app está desarrollada con Kotlin Multiplatform y compila para Android e iOS.

**¿Mis documentos están seguros?**
Sí. Los documentos se almacenan en Supabase con encriptación y políticas RLS que garantizan que solo tú y las personas autorizadas pueden acceder.

---

## 6. Contacto y Soporte

Para soporte técnico o sugestões:
- Email: soporte@lexsign.com
- GitHub: https://github.com/tu-usuario/lexsign

---

## 7. Créditos

Desarrollado con:
- Kotlin Multiplatform
- Jetpack Compose
- Supabase
- CAMERFIRMA

Versión: 1.0.0
Fecha: Mayo 2026
