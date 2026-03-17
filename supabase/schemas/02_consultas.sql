-- ============================================================
-- Schema 02: consultas + consulta_abogados
-- RF3: Solicitud y gestión de consultas legales
-- RF8: Búsqueda y filtrado de consultas
-- ============================================================

create table if not exists public.consultas (
  id            uuid primary key default gen_random_uuid(),
  cliente_id    uuid not null references public.users(id) on delete cascade,
  estado        text not null check (estado in ('abierta', 'en_curso', 'cerrada')) default 'abierta',
  area_practica text not null,
  descripcion   text not null,
  created_at    timestamptz default now(),
  updated_at    timestamptz default now()
);

-- Tabla intermedia para asignar uno o varios abogados a una consulta (RF3)
create table if not exists public.consulta_abogados (
  consulta_id   uuid not null references public.consultas(id) on delete cascade,
  abogado_id    uuid not null references public.users(id)    on delete cascade,
  asignado_en   timestamptz default now(),
  primary key (consulta_id, abogado_id)
);

-- ── Índices ──────────────────────────────────────────────────
create index if not exists consultas_cliente_idx  on public.consultas (cliente_id);
create index if not exists consultas_estado_idx   on public.consultas (estado);
create index if not exists consultas_area_idx     on public.consultas (area_practica);

-- ── Row Level Security ────────────────────────────────────────
alter table public.consultas         enable row level security;
alter table public.consulta_abogados enable row level security;

-- Clientes ven sus propias consultas; abogados ven las que les están asignadas
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

-- ── Trigger updated_at ──────────────────────────────────────
create trigger consultas_updated_at
  before update on public.consultas
  for each row execute function public.update_updated_at();
