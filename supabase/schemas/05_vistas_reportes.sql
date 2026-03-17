-- ============================================================
-- Schema 05: Vistas para reportes (RF13)
-- Solo accesibles por rol administrador
-- ============================================================

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
