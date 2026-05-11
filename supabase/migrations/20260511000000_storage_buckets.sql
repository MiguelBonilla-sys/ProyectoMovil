-- Storage Buckets para LexSign
-- Ejecutar en Supabase SQL Editor

-- Crear bucket para documentossubidos por usuarios
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES ('documentos', 'documentos', true, 10485760, ARRAY['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'])
ON CONFLICT (id) DO UPDATE SET
    public = true,
    file_size_limit = 10485760,
    allowed_mime_types = ARRAY['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];

-- Crear bucket para plantillas
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES ('plantillas', 'plantillas', true, 5242880, ARRAY['application/pdf'])
ON CONFLICT (id) DO UPDATE SET
    public = true,
    file_size_limit = 5242880,
    allowed_mime_types = ARRAY['application/pdf'];

-- Crear políticas RLS permisivas para documentos
DROP POLICY IF EXISTS "Permitir upload documentos" ON storage.objects;
CREATE POLICY "Permitir upload documentos" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'documentos');

DROP POLICY IF EXISTS "Permitir lectura documentos publicos" ON storage.objects;
CREATE POLICY "Permitir lectura documentos publicos" ON storage.objects
    FOR SELECT USING (bucket_id = 'documentos');

DROP POLICY IF EXISTS "Permitir delete documentos" ON storage.objects;
CREATE POLICY "Permitir delete documentos" ON storage.objects
    FOR DELETE USING (bucket_id = 'documentos');

-- Crear políticas RLS permisivas para plantillas
DROP POLICY IF EXISTS "Permitir upload plantillas" ON storage.objects;
CREATE POLICY "Permitir upload plantillas" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'plantillas');

DROP POLICY IF EXISTS "Permitir lectura plantillas publicos" ON storage.objects;
CREATE POLICY "Permitir lectura plantillas publicos" ON storage.objects
    FOR SELECT USING (bucket_id = 'plantillas');

DROP POLICY IF EXISTS "Permitir delete plantillas" ON storage.objects;
CREATE POLICY "Permitir delete plantillas" ON storage.objects
    FOR DELETE USING (bucket_id = 'plantillas');

-- Verificar que se crearon
SELECT id, name, public FROM storage.buckets;
