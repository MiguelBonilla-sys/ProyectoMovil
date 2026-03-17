-- ============================================================
-- Schema 03: documentos
-- RF4: Carga y gestión de documentos legales
-- RF5: Firma digital
-- RF6: Plantillas de documentos
-- RF7: Descarga y acceso
-- ============================================================

create table if not exists public.documentos (
  id             uuid primary key default gen_random_uuid(),
  consulta_id    uuid references public.consultas(id) on delete set null,
  nombre         text not null,
  url            text not null,              -- Supabase Storage URL
  tipo           text not null,              -- 'contrato','poder','acuerdo_confidencialidad','otro'
  es_plantilla   boolean not null default false,
  estado_firma   text not null check (estado_firma in ('pendiente', 'firmado', 'rechazado')) default 'pendiente',
  subido_por     uuid references public.users(id) on delete set null,
  ip_subida      text,
  sello_tiempo   timestamptz default now(),
  created_at     timestamptz default now()
);

-- ── Índices ──────────────────────────────────────────────────
create index if not exists documentos_consulta_idx     on public.documentos (consulta_id);
create index if not exists documentos_plantilla_idx    on public.documentos (es_plantilla);
create index if not exists documentos_estado_firma_idx on public.documentos (estado_firma);
create index if not exists documentos_subido_por_idx   on public.documentos (subido_por);

-- ── Row Level Security ────────────────────────────────────────
alter table public.documentos enable row level security;

-- Todos pueden leer plantillas
create policy "Lectura plantillas públicas"
  on public.documentos for select
  using (
    es_plantilla = true
    or subido_por = auth.uid()
    or consulta_id in (
      select id from public.consultas
      where cliente_id = auth.uid()
    )
    or consulta_id in (
      select consulta_id from public.consulta_abogados
      where abogado_id = auth.uid()
    )
  );

create policy "Inserción de documentos"
  on public.documentos for insert
  with check (subido_por = auth.uid());

create policy "Actualización de documentos"
  on public.documentos for update
  using (subido_por = auth.uid());

-- ── Supabase Storage Buckets (ejecutar manualmente desde Dashboard) ──
-- bucket: "documentos"  → archivos privados por consulta
-- bucket: "plantillas"  → plantillas públicas de descarga
