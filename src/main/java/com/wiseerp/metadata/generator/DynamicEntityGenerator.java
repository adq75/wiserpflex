package com.wiseerp.metadata.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DynamicEntityGenerator {
    private final JdbcTemplate jdbcTemplate;
    private final MetadataChangeLogRepository changeLogRepository;
    private static final Logger log = LoggerFactory.getLogger(DynamicEntityGenerator.class);
    private volatile Boolean isPostgresCache = null;

    // Safety limits
    private static final int MAX_COLUMNS = 200;

    private static final Set<String> ALLOWED_TYPES = Set.of("STRING", "INTEGER", "DECIMAL", "BOOLEAN", "DATE", "DATETIME", "JSON", "BINARY");

    public DynamicEntityGenerator(JdbcTemplate jdbcTemplate, MetadataChangeLogRepository changeLogRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.changeLogRepository = changeLogRepository;
    }

    public String buildCreateTableSql(UUID tenantId, EntityDefinition def) {
        String schema = schemaName(tenantId);
        String tableRaw = def.getName();
        if (tableRaw == null) throw new IllegalArgumentException("Entity name is null");
        String table = tableRaw.toLowerCase(Locale.ROOT);
        if (!table.matches("^[a-z][a-z0-9_]{1,99}$")) {
            throw new IllegalArgumentException("Invalid entity name (must be lowercase alnum/underscore): " + tableRaw);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(schema).append('.').append(table).append(" (\n");
        sb.append("  id UUID PRIMARY KEY,\n");
        sb.append("  tenant_id UUID NOT NULL,\n");

        JsonNode fields = def.getFields();
        int colCount = 0;
        if (fields != null && fields.isArray()) {
            Iterator<JsonNode> it = fields.elements();
            while (it.hasNext()) {
                JsonNode f = it.next();
                String nameRaw = f.path("name").asText(null);
                if (nameRaw == null) throw new IllegalArgumentException("Field name is null in entity " + tableRaw);
                String name = nameRaw.toLowerCase(Locale.ROOT);
                if (!name.matches("^[a-z][a-z0-9_]{0,99}$")) {
                    throw new IllegalArgumentException("Invalid field name (must be lowercase alnum/underscore): " + nameRaw);
                }
                String dt = f.path("dataType").asText("STRING").toUpperCase(Locale.ROOT);
                if (!ALLOWED_TYPES.contains(dt)) {
                    dt = "STRING"; // fallback
                }
                sb.append("  ").append(name).append(' ').append(sqlTypeFor(dt));
                if (it.hasNext()) sb.append(',');
                sb.append('\n');
                colCount++;
                if (colCount > MAX_COLUMNS) throw new IllegalArgumentException("Too many columns in entity " + tableRaw + ". Limit=" + MAX_COLUMNS);
            }
        }

        sb.append(");");
        return sb.toString();
    }

    public void createTable(UUID tenantId, EntityDefinition def, boolean dryRun) {
        // validate metadata first
        validateDefinition(def);

        String sql = buildCreateTableSql(tenantId, def);
        if (dryRun) return;
        // ensure tenant schema exists
        String schema = schemaName(tenantId);
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        } catch (Exception ignored) {
            // some DBs may not support CREATE SCHEMA; ignore and continue
        }
        // Log the DDL for traceability
        try {
            log.info("Executing DDL for tenant {}: {}", tenantId, sql.replaceAll("\n", " "));
        } catch (Exception ignored) {}

        // If running against Postgres, ensure JSON type uses JSONB
        if (isPostgres()) {
            sql = sql.replaceAll("\\bTEXT\\b", "JSONB");
        }

        jdbcTemplate.execute(sql);

        // record audit
        try {
            MetadataChangeLog log = MetadataChangeLog.builder()
                    .id(UUID.randomUUID())
                    .tenantId(tenantId)
                    .entityDefinitionId(def.getId())
                    .changeType("CREATE_TABLE")
                    .changePayload((JsonNode) null)
                    .changedBy("system")
                    .build();
            changeLogRepository.save(log);
        } catch (Exception ignored) {
            // best-effort audit; do not fail DDL on audit write errors
        }
    }

    public void validateDefinition(EntityDefinition def) {
        if (def == null) throw new IllegalArgumentException("EntityDefinition is null");
        String name = def.getName();
        if (name == null || !name.matches("^[A-Za-z][A-Za-z0-9_]{1,99}$")) {
            throw new IllegalArgumentException("Invalid entity name: " + name);
        }

        var fields = def.getFields();
        if (fields == null || !fields.isArray()) return;
        for (var it = fields.elements(); it.hasNext(); ) {
            var f = it.next();
            String fname = f.path("name").asText(null);
            String dt = f.path("dataType").asText("STRING").toUpperCase();
            if (fname == null || !fname.matches("^[A-Za-z][A-Za-z0-9_]{0,99}$")) {
                throw new IllegalArgumentException("Invalid field name: " + fname);
            }
            if (!ALLOWED_TYPES.contains(dt)) {
                throw new IllegalArgumentException("Invalid data type for field " + fname + ": " + dt);
            }
        }
    }

    private String schemaName(UUID tenantId) {
        return "tenant_" + tenantId.toString().replace('-', '_');
    }

    private String sqlTypeFor(String dt) {
        String datatype = dt == null ? "STRING" : dt.toUpperCase(Locale.ROOT);
        return switch (datatype) {
            case "INTEGER" -> "INTEGER";
            case "DECIMAL" -> "NUMERIC";
            case "BOOLEAN" -> "BOOLEAN";
            case "DATE" -> "DATE";
            case "DATETIME" -> "TIMESTAMP";
            case "JSON" -> {
                // prefer JSONB on Postgres, otherwise TEXT
                if (isPostgres()) yield "JSONB";
                else yield "TEXT"; // keep TEXT for compatibility in H2
            }
            case "BINARY" -> "BYTEA";
            default -> "VARCHAR(1024)";
        };
    }

    private boolean isPostgres() {
        if (isPostgresCache != null) return isPostgresCache;
        try (var conn = jdbcTemplate.getDataSource().getConnection()) {
            String name = conn.getMetaData().getDatabaseProductName();
            boolean isPg = name != null && name.toLowerCase(Locale.ROOT).contains("postgres");
            isPostgresCache = isPg;
            return isPg;
        } catch (Exception ex) {
            isPostgresCache = false;
            return false;
        }
    }
}
