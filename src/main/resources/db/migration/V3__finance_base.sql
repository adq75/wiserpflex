-- Migration: finance base tables (minimal)
CREATE TABLE IF NOT EXISTS accounts (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  code varchar(100) NOT NULL,
  name varchar(255) NOT NULL,
  account_type varchar(50),
  custom_fields jsonb,
  created_at timestamptz,
  updated_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_accounts_tenant_id ON accounts (tenant_id);

CREATE TABLE IF NOT EXISTS journal_entries (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  entry_date timestamptz NOT NULL,
  description text,
  created_at timestamptz,
  updated_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_journal_entries_tenant_id ON journal_entries (tenant_id);

CREATE TABLE IF NOT EXISTS journal_lines (
  id uuid PRIMARY KEY,
  tenant_id uuid NOT NULL,
  journal_entry_id uuid,
  account_id uuid,
  amount numeric,
  description text
);

CREATE INDEX IF NOT EXISTS idx_journal_lines_tenant_id ON journal_lines (tenant_id);
