-- ============================================================
-- Migración inicial: LexSign MVP — Cloud-First Supabase
-- Fecha: 2026-03-16
-- Cubre: RF1, RF2, RF3, RF4, RF5, RF6, RF7, RF8, RF10, RF11, RF13
-- ============================================================

-- ── 01: Tabla users ──────────────────────────────────────────
create table if not exists public.users (
  id                   uuid primary key default gen_random_uuid(),
  nombre               text not null,
  email                text not null unique,
  password             text not null,            -- hash PBKDF2
  tipo                 text not null check (tipo in ('cliente', 'abogado', 'administrador')),
  tarjeta              text default '',           -- tarjeta profesional (abogados)
  rol                  text not null default 'cliente' check (rol in ('cliente', 'abogado', 'administrador')),

  -- Campos cliente (RF1)
  preferencias_areas   text[] default '{}',
  tipo_tramites        text[] default '{}',

  -- Campos abogado (RF2)
  especialidad         text,
  experiencia          integer,
  descripcion          text,
  telefono             text,
  calificacion_promedio numeric(3,2) default 0.0,
  foto_url             text,

  created_at           timestamptz default now(),
  updated_at           timestamptz default now()
);

create index if not exists users_email_idx  on public.users (email);
create index if not exists users_tipo_idx   on public.users (tipo);

alter table public.users enable row level security;

create policy "Lectura propia"
  on public.users for select
  using (true);

create policy "Actualización propia"
  on public.users for update
  using (auth.uid() = id);

create policy "Inserción pública"
  on public.users for insert
  with check (true);

create or replace function public.update_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger users_updated_at
  before update on public.users
  for each row execute function public.update_updated_at();


-- ── 02: Consultas ────────────────────────────────────────────
create table if not exists public.consultas (
  id            uuid primary key default gen_random_uuid(),
  cliente_id    uuid not null references public.users(id) on delete cascade,
  estado        text not null check (estado in ('abierta', 'en_curso', 'cerrada')) default 'abierta',
  area_practica text not null,
  descripcion   text not null,
  created_at    timestamptz default now(),
  updated_at    timestamptz default now()
);

create table if not exists public.consulta_abogados (
  consulta_id   uuid not null references public.consultas(id) on delete cascade,
  abogado_id    uuid not null references public.users(id)    on delete cascade,
  asignado_en   timestamptz default now(),
  primary key (consulta_id, abogado_id)
);

create index if not exists consultas_cliente_idx  on public.consultas (cliente_id);
create index if not exists consultas_estado_idx   on public.consultas (estado);
create index if not exists consultas_area_idx     on public.consultas (area_practica);

alter table public.consultas         enable row level security;
alter table public.consulta_abogados enable row level security;

create policy "Clientes ven sus consultas"
  on public.consultas for select
  using (
    cliente_id = auth.uid()
    or exists (
      select 1 from public.consulta_abogados
      where consulta_id = id and abogado_id = auth.uid()
    )
  );

create policy "Clientes crean consultas"
  on public.consultas for insert
  with check (cliente_id = auth.uid());

create policy "Actualización de consultas"
  on public.consultas for update
  using (
    cliente_id = auth.uid()
    or exists (
      select 1 from public.consulta_abogados
      where consulta_id = id and abogado_id = auth.uid()
    )
  );

create policy "Lectura consulta_abogados"
  on public.consulta_abogados for select
  using (true);

create policy "Inserción consulta_abogados"
  on public.consulta_abogados for insert
  with check (true);

create trigger consultas_updated_at
  before update on public.consultas
  for each row execute function public.update_updated_at();


-- ── 03: Documentos ───────────────────────────────────────────
create table if not exists public.documentos (
  id             uuid primary key default gen_random_uuid(),
  consulta_id    uuid references public.consultas(id) on delete set null,
  nombre         text not null,
  url            text not null,
  tipo           text not null,
  es_plantilla   boolean not null default false,
  estado_firma   text not null check (estado_firma in ('pendiente', 'firmado', 'rechazado')) default 'pendiente',
  subido_por     uuid references public.users(id) on delete set null,
  ip_subida      text,
  sello_tiempo   timestamptz default now(),
  created_at     timestamptz default now()
);

create index if not exists documentos_consulta_idx     on public.documentos (consulta_id);
create index if not exists documentos_plantilla_idx    on public.documentos (es_plantilla);
create index if not exists documentos_estado_firma_idx on public.documentos (estado_firma);
create index if not exists documentos_subido_por_idx   on public.documentos (subido_por);

alter table public.documentos enable row level security;

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


-- ── 04: Notificaciones ───────────────────────────────────────
create table if not exists public.notificaciones (
  id          uuid primary key default gen_random_uuid(),
  usuario_id  uuid not null references public.users(id) on delete cascade,
  tipo        text not null check (tipo in ('consulta_nueva', 'documento_firmado', 'estado_cambio', 'sistema')),
  titulo      text not null,
  mensaje     text not null,
  leida       boolean not null default false,
  created_at  timestamptz default now()
);

create index if not exists notificaciones_usuario_idx on public.notificaciones (usuario_id);
create index if not exists notificaciones_leida_idx   on public.notificaciones (leida);

alter table public.notificaciones enable row level security;

create policy "Lectura propia notificaciones"
  on public.notificaciones for select
  using (usuario_id = auth.uid());

create policy "Actualización propia notificaciones"
  on public.notificaciones for update
  using (usuario_id = auth.uid());

create policy "Inserción notificaciones"
  on public.notificaciones for insert
  with check (true);


-- ── 05: Vistas para reportes (RF13) ─────────────────────────
create or replace view public.reporte_documentos_firmados as
select
  date_trunc('month', created_at) as mes,
  count(*) as total_firmados
from public.documentos
where estado_firma = 'firmado'
group by 1
order by 1 desc;

create or replace view public.reporte_consultas_abogado as
select
  u.id           as abogado_id,
  u.nombre       as abogado_nombre,
  u.especialidad,
  count(ca.consulta_id) as total_consultas,
  count(case when c.estado = 'cerrada' then 1 end) as consultas_cerradas
from public.users u
left join public.consulta_abogados ca on ca.abogado_id = u.id
left join public.consultas c          on c.id = ca.consulta_id
where u.tipo = 'abogado'
group by u.id, u.nombre, u.especialidad
order by total_consultas desc;

create or replace view public.reporte_resumen_general as
select
  (select count(*) from public.users where tipo = 'cliente')  as total_clientes,
  (select count(*) from public.users where tipo = 'abogado')  as total_abogados,
  (select count(*) from public.consultas)                     as total_consultas,
  (select count(*) from public.consultas where estado = 'abierta') as consultas_abiertas,
  (select count(*) from public.documentos)                    as total_documentos,
  (select count(*) from public.documentos where es_plantilla = true) as total_plantillas;
