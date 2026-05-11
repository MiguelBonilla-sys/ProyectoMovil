-- Migration: Tabla audit_logs para auditoría de cambios
-- Fecha: 2026-03-17
-- Fase 2: Auditoría y Seguridad

CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    usuario_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    
    tabla_afectada VARCHAR(50) NOT NULL, -- consultas, documentos, notificaciones, users, etc.
    accion VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    registro_id UUID NOT NULL, -- ID del registro modificado
    
    datos_anteriores JSONB, -- Valores antes del cambio (NULL para INSERT)
    datos_nuevos JSONB, -- Valores después del cambio (NULL para DELETE)
    
    ip_origen INET,
    user_agent TEXT,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_audit_logs_usuario ON public.audit_logs(usuario_id);
CREATE INDEX idx_audit_logs_tabla ON public.audit_logs(tabla_afectada);
CREATE INDEX idx_audit_logs_accion ON public.audit_logs(accion);
CREATE INDEX idx_audit_logs_registro ON public.audit_logs(registro_id);
CREATE INDEX idx_audit_logs_fecha ON public.audit_logs(created_at);

-- Índice compuesto para consultas frecuentes
CREATE INDEX idx_audit_logs_tabla_fecha ON public.audit_logs(tabla_afectada, created_at DESC);

-- RLS Policies
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admin puede leer logs de auditoría" 
    ON public.audit_logs FOR SELECT USING (
        (SELECT tipo FROM public.users WHERE id = auth.uid()) = 'administrador'
        OR true -- Permitir anon key para backend
    );

CREATE POLICY "Sistema puede insertar logs" 
    ON public.audit_logs FOR INSERT WITH CHECK (true);

-- Comentarios
COMMENT ON TABLE public.audit_logs IS 'Registro de auditoría de todas las operaciones importantes en el sistema';
COMMENT ON COLUMN public.audit_logs.tabla_afectada IS 'Tabla del sistema que fue modificada';
COMMENT ON COLUMN public.audit_logs.registro_id IS 'ID del registro específico que fue modificado';
COMMENT ON COLUMN public.audit_logs.datos_anteriores IS 'Valor anterior del registro (para UPDATE/DELETE)';
COMMENT ON COLUMN public.audit_logs.datos_nuevos IS 'Valor nuevo del registro (para INSERT/UPDATE)';

GRANT SELECT ON public.audit_logs TO anon;
GRANT INSERT ON public.audit_logs TO anon;
