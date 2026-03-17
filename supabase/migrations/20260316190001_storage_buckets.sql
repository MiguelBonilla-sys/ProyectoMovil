-- ============================================================
-- Migración: crear Storage buckets para documentos y plantillas
-- ============================================================

-- Bucket privado para documentos de consultas (RF4, RF6)
insert into storage.buckets (id, name, public)
values ('documentos', 'documentos', false)
on conflict (id) do nothing;

-- Bucket público para plantillas descargables (RF5)
insert into storage.buckets (id, name, public)
values ('plantillas', 'plantillas', true)
on conflict (id) do nothing;

-- RLS: usuarios autenticados pueden subir a documentos
create policy "Subida autenticada documentos"
  on storage.objects for insert
  to authenticated
  with check (bucket_id = 'documentos');

-- RLS: usuarios autenticados pueden leer sus documentos
create policy "Lectura propia documentos"
  on storage.objects for select
  to authenticated
  using (bucket_id = 'documentos' and auth.uid()::text = (storage.foldername(name))[1]);

-- RLS: todos pueden leer plantillas públicas
create policy "Lectura pública plantillas"
  on storage.objects for select
  to public
  using (bucket_id = 'plantillas');

-- RLS: solo autenticados pueden subir plantillas
create policy "Subida plantillas autenticados"
  on storage.objects for insert
  to authenticated
  with check (bucket_id = 'plantillas');
