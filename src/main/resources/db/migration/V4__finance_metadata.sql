-- Migration: finance metadata definitions (system-level templates)
INSERT INTO system.entity_definitions (id, tenant_id, name, display_name, category, schema_definition, fields, version, is_active, created_at)
VALUES
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'Account', 'Account', 'MASTER', '{}'::jsonb,
    '[{"name":"code","dataType":"STRING"},{"name":"name","dataType":"STRING"},{"name":"accountType","dataType":"STRING"},{"name":"customFields","dataType":"JSON"}]'::jsonb,
    'v1', true, now()),
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'JournalEntry', 'Journal Entry', 'TRANSACTION', '{}'::jsonb,
    '[{"name":"entryDate","dataType":"DATETIME"},{"name":"description","dataType":"STRING"}]'::jsonb,
    'v1', true, now()),
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'JournalLine', 'Journal Line', 'TRANSACTION', '{}'::jsonb,
    '[{"name":"journalEntryId","dataType":"UUID"},{"name":"accountId","dataType":"UUID"},{"name":"amount","dataType":"DECIMAL"},{"name":"description","dataType":"STRING"}]'::jsonb,
    'v1', true, now()),
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'FiscalPeriod', 'Fiscal Period', 'REFERENCE', '{}'::jsonb,
    '[{"name":"startDate","dataType":"DATE"},{"name":"endDate","dataType":"DATE"},{"name":"name","dataType":"STRING"}]'::jsonb,
    'v1', true, now()),
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'FinancialReport', 'Financial Report', 'REFERENCE', '{}'::jsonb,
    '[{"name":"name","dataType":"STRING"},{"name":"generatedAt","dataType":"DATETIME"},{"name":"content","dataType":"JSON"}]'::jsonb,
    'v1', true, now());

-- Note: These are system-level templates (tenant_id=NULL). MetadataVerificationService
-- treats tenant_id NULL entries as applicable to all tenants.
