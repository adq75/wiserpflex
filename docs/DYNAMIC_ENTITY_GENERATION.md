# Dynamic Entity Generation - Design Summary

Overview
--------
This document describes the hybrid approach for dynamic entity generation used by WiseERP.

Key points
- Hybrid DDL: baseline migrations for system objects; controlled runtime DDL for tenant-scoped entities.
- API: start with a Generic CRUD controller that operates on registered entity metadata.
- Safety: strict metadata validation, dry-run mode, audit logging, and a production feature-flag.

Safety checklist
- Validate field names and data types against a whitelist.
- Always create tables in tenant schema: `tenant_{tenantId}`.
- Include `tenant_id UUID NOT NULL` column by default (defense-in-depth).
- Record every DDL action into `metadata_change_log` via existing audit service.
- Provide `preview`/`dry-run` admin endpoint for generated DDL.

Phased rollout
1. Add generator scaffold (validation + SQL builder + executor).
2. Add Generic CRUD controller to operate on generated entities.
3. Add integration tests and dry-run endpoints.
4. Optionally add runtime endpoint registration or build-time codegen.

Files introduced by scaffold
- `src/main/java/com/wiseerp/metadata/generator/DynamicEntityGenerator.java`
- `src/main/java/com/wiseerp/metadata/generator/ApiGeneratorService.java`
- `src/main/java/com/wiseerp/metadata/controller/GeneratedEntityController.java`
- `src/main/resources/db/migration/V2__dynamic_entities.sql` (template)

Notes
- This design preserves existing code and adds non-destructive scaffolding. Unit and integration tests will verify behavior before wider rollout.
