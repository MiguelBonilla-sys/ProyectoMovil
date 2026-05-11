-- Migration: Agregar campos de firma a tabla documentos
-- Fecha: 2026-03-17

-- Agregar columnas nuevas a documentos
ALTER TABLE public.documentos 
ADD COLUMN IF NOT EXISTS proceso_firma_id UUID REFERENCES public.procesos_firma(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS sello_tiempo_verificado TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS tipo_firma VARCHAR(50), -- simple, avanzada
ADD COLUMN IF NOT EXISTS cadena_firmas JSONB DEFAULT '[]'::jsonb, -- Array de FirmaRegistro
ADD COLUMN IF NOT EXISTS url_documento_firmado TEXT,
ADD COLUMN IF NOT EXISTS codigo_verificacion VARCHAR(255);

-- Índices
CREATE INDEX IF NOT EXISTS idx_documentos_proceso_firma ON public.documentos(proceso_firma_id);
CREATE INDEX IF NOT EXISTS idx_documentos_codigo_verificacion ON public.documentos(codigo_verificacion);

-- Comentarios para documentación
COMMENT ON COLUMN public.documentos.proceso_firma_id IS 'FK a procesos_firma.id - vincula documento al flujo de firma';
COMMENT ON COLUMN public.documentos.sello_tiempo_verificado IS 'Timestamp verificado desde CAMERFIRMA';
COMMENT ON COLUMN public.documentos.tipo_firma IS 'Tipo de firma: simple o avanzada según Decreto 2364';
COMMENT ON COLUMN public.documentos.cadena_firmas IS 'Historial de firmas en formato JSONB (array de FirmaRegistro)';
COMMENT ON COLUMN public.documentos.url_documento_firmado IS 'URL en Supabase Storage del PDF firmado';
COMMENT ON COLUMN public.documentos.codigo_verificacion IS 'Código de verificación para terceros';
