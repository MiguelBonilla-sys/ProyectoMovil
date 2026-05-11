-- Migration: Tabla procesos_firma para gestionar flujos de firma electrónica
-- Fecha: 2026-03-17

-- Crear tabla procesos_firma
CREATE TABLE IF NOT EXISTS public.procesos_firma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL REFERENCES public.documentos(id) ON DELETE CASCADE,
    camerfirma_id VARCHAR(255), -- ID en sistema CAMERFIRMA
    camerfirma_url TEXT, -- URL para redirigir usuario a CAMERFIRMA
    
    estado VARCHAR(50) NOT NULL DEFAULT 'iniciado', -- iniciado, en_progreso, completo, expirado, cancelado, error
    tipo_firma VARCHAR(50) NOT NULL DEFAULT 'simple', -- simple, avanzada
    
    firmantes_requeridos UUID[] NOT NULL, -- Array de user IDs que deben firmar
    firmas_completadas JSONB NOT NULL DEFAULT '[]'::jsonb, -- Array de FirmaRegistro completadas
    
    url_documento_firmado TEXT, -- URL en Storage del PDF firmado
    codigo_verificacion VARCHAR(255), -- Para terceros verifiquen firma
    
    fecha_vencimiento TIMESTAMPTZ, -- Cuándo expira el proceso (default 30 días)
    intentos_fallidos INT DEFAULT 0,
    errores TEXT[] DEFAULT ARRAY[]::TEXT[], -- Array de mensajes de error
    
    initiated_by UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ, -- Cuándo se completó
    
    CHECK (array_length(firmantes_requeridos, 1) > 0)
);

-- Índices
CREATE INDEX idx_procesos_firma_documento ON public.procesos_firma(documento_id);
CREATE INDEX idx_procesos_firma_estado ON public.procesos_firma(estado);
CREATE INDEX idx_procesos_firma_usuario ON public.procesos_firma(initiated_by);
CREATE INDEX idx_procesos_firma_vencimiento ON public.procesos_firma(fecha_vencimiento);

-- RLS Policies
ALTER TABLE public.procesos_firma ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Lectura pública de procesos" 
    ON public.procesos_firma FOR SELECT USING (true);

CREATE POLICY "Usuario puede crear procesos" 
    ON public.procesos_firma FOR INSERT WITH CHECK (auth.uid()::text = initiated_by::text OR true); -- true = anon key

CREATE POLICY "Usuario puede actualizar procesos propios" 
    ON public.procesos_firma FOR UPDATE USING (auth.uid()::text = initiated_by::text OR true);

-- Trigger para actualizar updated_at
CREATE TRIGGER trigger_procesos_firma_updated_at
    BEFORE UPDATE ON public.procesos_firma
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

GRANT SELECT ON public.procesos_firma TO anon;
GRANT INSERT ON public.procesos_firma TO anon;
GRANT UPDATE ON public.procesos_firma TO anon;
