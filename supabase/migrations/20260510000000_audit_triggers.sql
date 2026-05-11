-- ============================================================
-- Migration: Triggers de auditoría automática
-- Fecha: 2026-05-10
-- Fase 2: Auditoría y Seguridad
-- ============================================================

-- Función genérica para registrar cambios en audit_logs
CREATE OR REPLACE FUNCTION public.audit_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
    audit_row audit_logs%ROWTYPE;
    user_id_param UUID;
    ip_param TEXT;
    ua_param TEXT;
BEGIN
    -- Obtener usuario actual
    BEGIN
        user_id_param := current_setting('app.current_user_id', true);
    EXCEPTION WHEN OTHERS THEN
        user_id_param := NULL;
    END;
    
    -- Obtener IP del cliente
    BEGIN
        ip_param := current_setting('app.client_ip', true);
    EXCEPTION WHEN OTHERS THEN
        ip_param := NULL;
    END;
    
    -- Obtener User Agent
    BEGIN
        ua_param := current_setting('app.client_user_agent', true);
    EXCEPTION WHEN OTHERS THEN
        ua_param := NULL;
    END;

    -- Construir el registro de auditoría
    audit_row.id := gen_random_uuid();
    audit_row.usuario_id := user_id_param;
    audit_row.registro_id := COALESCE(
        NEW.id,
        OLD.id,
        gen_random_uuid()
    );
    audit_row.ip_origen := ip_param::inet;
    audit_row.user_agent := ua_param;
    audit_row.created_at := NOW();

    -- Determinar acción y datos
    IF (TG_OP = 'INSERT') THEN
        audit_row.tabla_afectada := TG_TABLE_NAME;
        audit_row.accion := 'INSERT';
        audit_row.datos_nuevos := to_jsonb(NEW);
        audit_row.datos_anteriores := NULL;
    ELSIF (TG_OP = 'UPDATE') THEN
        audit_row.tabla_afectada := TG_TABLE_NAME;
        audit_row.accion := 'UPDATE';
        audit_row.datos_nuevos := to_jsonb(NEW);
        audit_row.datos_anteriores := to_jsonb(OLD);
    ELSIF (TG_OP = 'DELETE') THEN
        audit_row.tabla_afectada := TG_TABLE_NAME;
        audit_row.accion := 'DELETE';
        audit_row.datos_nuevos := NULL;
        audit_row.datos_anteriores := to_jsonb(OLD);
    END IF;

    -- Insertar en audit_logs
    INSERT INTO audit_logs VALUES (audit_row.*);

    -- Devolver la fila apropiada según la operación
    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger para consultas (INSERT, UPDATE, DELETE)
DROP TRIGGER IF EXISTS audit_consultas ON public.consultas;
CREATE TRIGGER audit_consultas
    AFTER INSERT OR UPDATE OR DELETE ON public.consultas
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();

-- Trigger para documentos (INSERT, UPDATE, DELETE)
DROP TRIGGER IF EXISTS audit_documentos ON public.documentos;
CREATE TRIGGER audit_documentos
    AFTER INSERT OR UPDATE OR DELETE ON public.documentos
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();

-- Trigger para users (INSERT, UPDATE - no DELETE por seguridad)
DROP TRIGGER IF EXISTS audit_users ON public.users;
CREATE TRIGGER audit_users
    AFTER INSERT OR UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();

-- Trigger para notificaciones (INSERT, UPDATE, DELETE)
DROP TRIGGER IF EXISTS audit_notificaciones ON public.notificaciones;
CREATE TRIGGER audit_notificaciones
    AFTER INSERT OR UPDATE OR DELETE ON public.notificaciones
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();

-- Trigger para procesos_firma (INSERT, UPDATE, DELETE)
DROP TRIGGER IF EXISTS audit_procesos_firma ON public.procesos_firma;
CREATE TRIGGER audit_procesos_firma
    AFTER INSERT OR UPDATE OR DELETE ON public.procesos_firma
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();

-- Trigger para firmas_registros (INSERT, UPDATE, DELETE)
DROP TRIGGER IF EXISTS audit_firmas_registros ON public.firmas_registros;
CREATE TRIGGER audit_firmas_registros
    AFTER INSERT OR UPDATE OR DELETE ON public.firmas_registros
    FOR EACH ROW EXECUTE FUNCTION public.audit_trigger_function();

-- Función para registrar auditoría manualmente (para casos especiales)
CREATE OR REPLACE FUNCTION public.log_audit(
    p_tabla TEXT,
    p_accion TEXT,
    p_registro_id UUID,
    p_datos_anteriores JSONB DEFAULT NULL,
    p_datos_nuevos JSONB DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    new_audit_id UUID;
BEGIN
    INSERT INTO audit_logs (
        id, usuario_id, tabla_afectada, accion, registro_id,
        datos_anteriores, datos_nuevos, ip_origen, user_agent
    )
    VALUES (
        gen_random_uuid(),
        NULLIF(current_setting('app.current_user_id', true), '')::UUID,
        p_tabla,
        p_accion,
        p_registro_id,
        p_datos_anteriores,
        p_datos_nuevos,
        NULLIF(current_setting('app.client_ip', true), '')::inet,
        NULLIF(current_setting('app.client_user_agent', true), '')
    )
    RETURNING id INTO new_audit_id;
    
    RETURN new_audit_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Habilitar función para uso anónimo (para la app)
GRANT EXECUTE ON FUNCTION public.log_audit TO anon;
