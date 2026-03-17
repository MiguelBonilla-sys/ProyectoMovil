-- ============================================================
-- Schema 04: notificaciones
-- RF10: Sistema de notificaciones
-- ============================================================

-- Migración: agregar columna rol a users (RF11)
alter table public.users
  add column if not exists rol text not null default 'cliente'
    check (rol in ('cliente', 'abogado', 'administrador'));

-- Crear tabla notificaciones
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
