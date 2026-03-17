-- ============================================================
-- Schema 01: users
-- Tabla principal de usuarios (clientes, abogados, admins)
-- ============================================================

create table if not exists public.users (
  id                   uuid primary key default gen_random_uuid(),
  nombre               text not null,
  email                text not null unique,
  password             text not null,            -- hash PBKDF2
  tipo                 text not null check (tipo in ('cliente', 'abogado', 'administrador')),
  tarjeta              text default '',           -- tarjeta profesional (abogados)

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

-- ── Índices ──────────────────────────────────────────────────
create index if not exists users_email_idx  on public.users (email);
create index if not exists users_tipo_idx   on public.users (tipo);

-- ── Row Level Security ────────────────────────────────────────
alter table public.users enable row level security;

-- Los usuarios pueden leer y actualizar su propio perfil
create policy "Lectura propia"
  on public.users for select
  using (true);   -- todos pueden leer perfiles (para buscar abogados)

create policy "Actualización propia"
  on public.users for update
  using (auth.uid() = id);

create policy "Inserción pública"
  on public.users for insert
  with check (true);  -- el registro es público

-- ── Trigger updated_at ──────────────────────────────────────
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
