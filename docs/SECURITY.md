# Seguridad LexSign

## Hashing de Contraseñas

Las contraseñas se hash-ean con PBKDF2-HMAC-SHA256:
- Iteraciones: 10000
- Salt: 16 bytes aleatorios
- Output: Base64

```kotlin
object SecurityUtils {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    
    fun hashPassword(password: String): String {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val hash = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(salt) + ":" + 
               Base64.getEncoder().encodeToString(hash)
    }
    
    fun verifyPassword(password: String, storedHash: String): Boolean {
        val (saltB64, hashB64) = storedHash.split(":")
        val salt = Base64.getDecoder().decode(saltB64)
        val expectedHash = Base64.getDecoder().decode(hashB64)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val actualHash = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        return expectedHash.contentEquals(actualHash)
    }
}
```

## Row Level Security (RLS)

Todas las tablas tienen RLS habilitado:

```sql
-- consultas: Solo cliente dueño o abogados asignados
CREATE POLICY "Lectura consultas"
    ON public.consultas FOR SELECT USING (
        cliente_id = auth.uid()
        OR EXISTS (SELECT 1 FROM consulta_abogados WHERE consulta_id = id AND abogado_id = auth.uid())
    );

-- documentos: Dueño o participantes de consulta
CREATE POLICY "Lectura documentos"
    ON public.documentos FOR SELECT USING (
        subido_por = auth.uid()
        OR es_plantilla = true
        OR consulta_id IN (...)
    );
```

## Auditoría

Triggers automáticos registran todos los cambios:

```sql
CREATE TRIGGER audit_consultas
    AFTER INSERT OR UPDATE OR DELETE ON public.consultas
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();
```

## Validación de Entrada

```kotlin
object ValidationUtils {
    fun isValidEmail(email: String): Boolean { ... }
    fun isValidPassword(password: String): Boolean { ... }
    fun isValidPhone(phone: String): Boolean { ... }
}
```

## Firmas Electrónicas (CAMERFIRMA)

- Certificado digital requerido
- Sello de tiempo RFC 3161
- No repudio
- Cadena de custodia en BD
