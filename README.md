# WiseERP (Phase 1)

Multi-tenant ERP foundation (schema-per-tenant) — Phase 1 artifacts.

Quick commands

```bash
# build
mvn -B clean package

# run tests
mvn -B test

# run locally (requires configured Postgres)
mvn spring-boot:run
```

Notes
- Multi-tenancy uses `search_path` per-connection (schema-per-tenant).
- Test bootstrap is restricted for fast controller tests; see `src/test/java/com/wiseerp/TestApplication.java`.

Tenant header
- To identify the tenant for requests, the application recognizes the HTTP header `X-Tenant-ID`.
- The header value should be the tenant schema identifier (for example: `tenant_01234567-89ab-cdef-0123-456789abcdef`) or simply the raw UUID in path-based APIs — controllers often accept `tenantId` path variable and code concatenates `tenant_` prefix when setting runtime context.
- Alternatively the header can be omitted if the tenant id is supplied as a request parameter `tenantId` or as a path variable.

Example:
```http
POST /api/tenant_01234567-89ab-cdef-0123-456789abcdef/finance/journal-entries
X-Tenant-ID: tenant_01234567-89ab-cdef-0123-456789abcdef
Content-Type: application/json

{ ... journal entry payload ... }
```

Repository
- Remote: https://github.com/adq75/wiserpflex.git
