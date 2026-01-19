-- Migration: create audit_log table for data-level auditing
CREATE TABLE IF NOT EXISTS system.audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    entity_name VARCHAR(200),
    entity_id UUID,
    action VARCHAR(50),
    payload JSONB,
    actor VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant ON system.audit_log(tenant_id);
