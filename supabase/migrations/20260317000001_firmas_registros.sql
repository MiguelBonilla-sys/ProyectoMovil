-- Migration: Tabla firmas_registros para registrar cada firma realizada
-- Fecha: 2026-03-17

CREATE TABLE IF NOT EXISTS public.firmas_registros (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    documento_id UUID NOT NULL REFERENCES public.documentos(id) ON DELETE CASCADE,
    proceso_firma_id UUID NOT NULL REFERENCES public.procesos_firma(id) ON DELETE CASCADE,
    
    firmante_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    
    firma_data TEXT NOT NULL, -- Base64 de la firma digital
    sello_tiempo TIMESTAMPTZ NOT NULL, -- Timestamp verificado de CAMERFIRMA
    
    ip_firmante INET,
    user_agent TEXT,
    
    certificado_id VARCHAR(255), -- ID del certificado usado en CAMERFIRMA
    algoritmo_firma VARCHAR(50) DEFAULT 'SHA256withRSA', -- Algoritmo usado
    
    estado VARCHAR(50) NOT NULL DEFAULT 'firmado', -- firmado, rechazado
    motivo_rechazo TEXT, -- Si fue rechazado, explicación
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_firmas_registros_documento ON public.firmas_registros(documento_id);
CREATE INDEX idx_firmas_registros_proceso ON public.firmas_registros(proceso_firma_id);
CREATE INDEX idx_firmas_registros_firmante ON public.firmas_registros(firmante_id);
CREATE INDEX idx_firmas_registros_estado ON public.firmas_registros(estado);
CREATE INDEX idx_firmas_registros_creados ON public.firmas_registros(created_at);

-- RLS Policies
ALTER TABLE public.firmas_registros ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Lectura pública de registros de firma" 
    ON public.firmas_registros FOR SELECT USING (true);

CREATE POLICY "Insertar registros de firma" 
    ON public.firmas_registros FOR INSERT WITH CHECK (true); -- anon key

GRANT SELECT ON public.firmas_registros TO anon;
GRANT INSERT ON public.firmas_registros TO anon;
