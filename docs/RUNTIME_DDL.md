# Runtime DDL Strategy and Operational Notes

This document explains the runtime DDL (CREATE TABLE per-tenant) approach used by the metadata
platform, safety controls, Postgres-specific behavior, testing guidance, and operational runbook
notes for applying and rolling back generated schema changes.

## Overview

- Purpose: allow safe, metadata-driven creation of tenant-scoped tables at runtime from
  `EntityDefinition.fields` without requiring compile-time schema migrations for each generated
  entity.
- Key components:
  - `DynamicEntityGenerator` — builds/executes CREATE TABLE DDL from an `EntityDefinition` JSON
    and records a `MetadataChangeLog` entry for audit.
  - `MetadataChangeLog` / `MetadataVersion` — best-effort audit and lightweight versioning.
  - Controller endpoints: `/api/system/metadata/entities/preview` (dry-run) and
    `/api/system/metadata/entities/apply` (execute DDL).

## Tenant Schema Naming

- Schema per tenant: `tenant_{tenantId}` where `tenantId` is the UUID with `-` replaced by `_`.
- Example: tenant 95ed... -> `tenant_95ed9979_866e_4ab0_88be_c7c4ef58065f`.

## Safety Controls (implemented in `DynamicEntityGenerator`)

- Identifier validation: entity and field names are normalized to lower-case and validated
  against a strict regex (letters, digits, underscores) to avoid SQL-injection or invalid
  identifiers.
- Allowed data types: whitelisted (`STRING`, `INTEGER`, `DECIMAL`, `BOOLEAN`, `DATE`,
  `DATETIME`, `JSON`, `BINARY`); unknown types are rejected at validation time.
- Column limits: `MAX_COLUMNS` (default 200) enforced to avoid unbounded DDL.
- Dry-run/Preview: build-only path returns generated SQL without executing; use this for review.
- Audit: generator records a `MetadataChangeLog` entry after successful DDL (best-effort; errors
  in audit writing do not block DDL execution).

## Postgres-specific behavior

- `DynamicEntityGenerator` autodetects Postgres and prefers `JSONB` for `JSON` typed fields;
  otherwise `TEXT` is used for H2/others.
- Integration tests use `it-postgres` Spring profile and the tests now drop tenant schemas after
  each IT run using `DROP SCHEMA IF EXISTS <schema> CASCADE` to keep the test DB clean.

## Permissions

- The DB user used by integration tests and runtime must have privileges to: `CREATE SCHEMA`,
  `CREATE TABLE`, `CREATE SEQUENCE` (if used), and `DROP` for cleanup tasks (or use a separate
  migration/ops account to run destructive commands).
- For production, consider separating DDL privilege: operator account performs `apply` after
  approval, or run generator with restricted rights and a migration job for heavy changes.

## Testing & CI

- Use `it-postgres` profile for Postgres ITs: `-Dspring.profiles.active=it-postgres`.
- Tests create tenant schemas and now drop them in `@AfterEach` to avoid test pollution.
- Add targeted unit tests for generator validation (invalid identifiers, column limits) and
  conversion helpers (done in the test suite).

## Rollback & Migration Notes

- Runtime DDL is intentionally lightweight: automatic `DROP TABLE` or destructive rollbacks are
  not performed by default. Versioning is recorded via `MetadataVersion` + `MetadataChangeLog`.
- For schema rollbacks, prefer an explicit migration path: record the reverse migration SQL in
  a controlled migration job (audit + manual approval), then run against target tenant(s).

## Operational Runbook (recommended)

1. Author metadata change and run `/preview` to inspect generated SQL.
2. Peer review the SQL and verify types/columns.
3. Run `/apply` in staging; run application-level tests and DB integrity checks.
4. If approved, run `/apply` in production for one tenant or small set; monitor logs & errors.
5. For wide changes, create a scheduled migration job that applies DDL in batches and records
   explicit rollback SQL where feasible.

## Logging & Monitoring

- DDL execution is logged (INFO) with tenant id and SQL (newlines collapsed). Monitor for
  repeated failures, audit write errors, and unusually large table definitions.

## Recommendations

- Keep `ALLOWED_TYPES` deliberate and extend only with careful mapping tests.
- Consider separating DDL execution into an operator workflow for high-sensitivity schemas.
- Back up target DB before applying wide schema changes; for Postgres, test restore from backup
  as part of change validation.

---
Last updated: 2026-01-15
