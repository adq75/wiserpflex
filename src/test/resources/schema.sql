CREATE SCHEMA IF NOT EXISTS system;
CREATE TABLE IF NOT EXISTS system.metadata_change_log (
    id UUID PRIMARY KEY,
    tenant_id UUID,
    entity_definition_id UUID,
    change_type VARCHAR(255),
    change_payload CLOB,
    changed_by VARCHAR(255),
    created_at TIMESTAMP
);
