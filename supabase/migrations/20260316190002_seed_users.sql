-- ============================================================
-- Seed: Usuarios de prueba para LexSign MVP
-- ============================================================
-- Contraseñas (PBKDF2-SHA256, 10k iter, Base64):
--   admin@lexsign.com     → Admin123!
--   cliente@test.com      → Cliente123!
--   abogado1@test.com     → Abogado123!
--   abogado2@test.com     → Abogado456!
-- ============================================================

insert into public.users
  (id, nombre, email, password, tipo, rol,
   especialidad, experiencia, descripcion, telefono,
   preferencias_areas, tipo_tramites)
values

-- ── Administrador ──────────────────────────────────────────
(
  'a0000000-0000-0000-0000-000000000001',
  'Administrador LexSign',
  'admin@lexsign.com',
  '4nOnGWYygz4O5HLw5lPERA==:0Yb6HEfbxcNbApHb1w9Fwkx8MyJOp5oijHddzUoR/2A=',
  'administrador',
  'administrador',
  null, null, null, null,
  '{}', '{}'
),

-- ── Cliente de prueba ──────────────────────────────────────
(
  'c0000000-0000-0000-0000-000000000001',
  'Carlos García',
  'cliente@test.com',
  'piwMBhO15Pe/mhNmCZxpsg==:NkKBfRIHWDkFRqd3KIYgnm50I74/S+MGuLdqldm81HU=',
  'cliente',
  'cliente',
  null, null, null, null,
  '{"Derecho Civil", "Derecho Laboral"}',
  '{"Contratos", "Consultas generales"}'
),

-- ── Abogado 1 ─────────────────────────────────────────────
(
  'b0000000-0000-0000-0000-000000000001',
  'Ana Martínez',
  'abogado1@test.com',
  'O+7fuAPm7K7PR4rVvSmYaw==:cLlPQhmoryxwYrCrjvbyHQt9bYwgHMEe13VQlYqSqtg=',
  'abogado',
  'abogado',
  'Derecho Civil',
  8,
  'Especialista en contratos civiles y litigios. 8 años de experiencia en el sector.',
  '+52 55 1234 5678',
  '{}', '{}'
),

-- ── Abogado 2 ─────────────────────────────────────────────
(
  'b0000000-0000-0000-0000-000000000002',
  'Roberto Sánchez',
  'abogado2@test.com',
  'AymM/E5t2DPjM+Wbt931vA==:BE0tjLVLbuCmcKwVCkRzxTYPhnByNGfjJOvnm+NSUTI=',
  'abogado',
  'abogado',
  'Derecho Laboral',
  5,
  'Abogado laboral con experiencia en asesoría empresarial y defensa de trabajadores.',
  '+52 55 9876 5432',
  '{}', '{}'
)

on conflict (email) do nothing;
