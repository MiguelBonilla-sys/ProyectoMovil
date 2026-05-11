-- Migration: Tabla password_reset_tokens para recuperación de contraseña
-- Fecha: 2026-03-17
-- Fase 1: Recuperación de Contraseña

CREATE TABLE IF NOT EXISTS public.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    user_id UUID NOT NULL UNIQUE REFERENCES public.users(id) ON DELETE CASCADE,
    
    token VARCHAR(255) NOT NULL UNIQUE, -- UUID como token
    
    expires_at TIMESTAMPTZ NOT NULL, -- Fecha de vencimiento (típicamente 15 minutos)
    used_at TIMESTAMPTZ, -- Cuando fue usado (NULL si no se ha usado)
    
    ip_origen INET, -- IP desde donde se solicitó el reset
    user_agent TEXT,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_password_reset_tokens_user ON public.password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON public.password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expires ON public.password_reset_tokens(expires_at);

-- RLS Policies
ALTER TABLE public.password_reset_tokens ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Lectura pública de tokens" 
    ON public.password_reset_tokens FOR SELECT USING (true);

CREATE POLICY "Insertar tokens" 
    ON public.password_reset_tokens FOR INSERT WITH CHECK (true); -- Sistema puede crear

CREATE POLICY "Actualizar tokens" 
    ON public.password_reset_tokens FOR UPDATE USING (true);

GRANT SELECT ON public.password_reset_tokens TO anon;
GRANT INSERT ON public.password_reset_tokens TO anon;
GRANT UPDATE ON public.password_reset_tokens TO anon;
GRANT DELETE ON public.password_reset_tokens TO anon;
