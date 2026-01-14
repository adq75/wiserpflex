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

Repository
- Remote: https://github.com/adq75/wiserpflex.git
