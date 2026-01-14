-- Initial schema: system and shared schemas + metadata tables

CREATE SCHEMA IF NOT EXISTS system;
CREATE SCHEMA IF NOT EXISTS shared;

-- Tenants registry (in system schema)
CREATE TABLE IF NOT EXISTS system.tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    schema_name VARCHAR(200) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_tenants_name ON system.tenants(name);

-- Entity definitions (metadata) stored in system schema
CREATE TABLE IF NOT EXISTS system.entity_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(255),
    category VARCHAR(50),
    schema_definition JSONB,
    fields JSONB,
    version VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_entity_definitions_tenant_name ON system.entity_definitions(tenant_id, name);
CREATE INDEX IF NOT EXISTS idx_entity_definitions_tenant ON system.entity_definitions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_entity_definitions_category ON system.entity_definitions(category);

-- Field definitions (metadata)
CREATE TABLE IF NOT EXISTS system.field_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_definition_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    is_required BOOLEAN DEFAULT false,
    default_value TEXT,
    validation_rules JSONB,
    ui_config JSONB,
    order_index INTEGER,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_field_def_entity ON system.field_definitions(entity_definition_id);

-- Metadata change audit
CREATE TABLE IF NOT EXISTS system.metadata_change_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    entity_definition_id UUID,
    change_type VARCHAR(50),
    change_payload JSONB,
    changed_by VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_metadata_change_tenant ON system.metadata_change_log(tenant_id);

-- Example shared table for common lookups
CREATE TABLE IF NOT EXISTS shared.currencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_currencies_code ON shared.currencies(code);

-- Ensure uuid extension exists
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Notes: tenant-specific schema creation will be handled by metadata hooks (on_create)
