package com.wiseerp.metadata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.metadata.generator.ApiGeneratorService;
import com.wiseerp.metadata.model.EntityDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/{tenantId}/generated")
@RequiredArgsConstructor
public class GeneratedEntityFullController {

    private final ApiGeneratorService apiGeneratorService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.wiseerp.audit.service.AuditService auditService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.wiseerp.events.DomainEventPublisher eventPublisher;

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-z][a-z0-9_]{1,99}$");

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.wiseerp.iam.service.AuthorizationService authorizationService;

    @PostMapping("/{entityName}")
    public ResponseEntity<?> create(@PathVariable("tenantId") UUID tenantId,
                                    @PathVariable("entityName") String entityName,
                                    @RequestBody Map<String, Object> payload) throws Exception {

        Optional<EntityDefinition> defOpt = apiGeneratorService.get(entityName);
        if (defOpt.isEmpty()) return ResponseEntity.notFound().build();
        EntityDefinition def = defOpt.get();

        if (!authorize("write", def)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        // Validate entity identifiers to match generator rules and avoid unsafe identifiers
        if (!isValidIdentifier(entityName) || !isValidIdentifier(def.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity name"));
        }

        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        String table = def.getName().toLowerCase();

        var cols = getTableColumns(schema, table);
        UUID id = UUID.randomUUID();

        var fieldTypeMap = getFieldTypeMap(def);

        // validate payload against EntityDefinition fields (if present)
        ResponseEntity<?> maybe = validatePayload(payload, def, true);
        if (maybe != null) return maybe;

        // build insert using payload keys that map to actual columns, converting types
        var insertCols = new java.util.ArrayList<String>();
        var insertParams = new java.util.ArrayList<Object>();
        insertCols.add("id"); insertParams.add(id);
        insertCols.add("tenant_id"); insertParams.add(tenantId);

        for (var e : payload.entrySet()) {
            String k = e.getKey();
            if (cols.contains(k)) {
                String dtype = fieldTypeMap.getOrDefault(k, "STRING");
                Object converted = convertValue(e.getValue(), dtype);
                insertCols.add(k);
                insertParams.add(converted);
            }
        }

        // if no payload column matched, try to store into `data` column if present
        if (insertCols.size() == 2) {
            if (cols.contains("data")) {
                insertCols.add("data");
                insertParams.add(objectMapper.writeValueAsString(payload));
            } else {
                throw new IllegalArgumentException("No matching columns in target table and no 'data' column present");
            }
        }

        String colList = String.join(", ", insertCols);
        String placeholders = String.join(", ", java.util.Collections.nCopies(insertCols.size(), "?"));
        String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, table, colList, placeholders);

        jdbcTemplate.update(sql, insertParams.toArray());

        // record audit and publish event (best-effort)
        try {
            com.wiseerp.audit.model.AuditLog entry = com.wiseerp.audit.model.AuditLog.builder()
                .tenantId(tenantId)
                .entityName(table)
                .entityId(id)
                .action("CREATE")
                .payload(objectMapper.valueToTree(payload))
                .actor(principalName())
                .build();
            if (auditService != null) auditService.log(entry);
            if (eventPublisher != null) eventPublisher.publish(new com.wiseerp.events.DomainEvent(tenantId, table, id, "CREATE", objectMapper.valueToTree(payload), java.time.Instant.now()));
        } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of("id", id));
    }

    private String principalName() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return "system";
            var p = auth.getPrincipal();
            if (p instanceof String) return (String) p;
            try { return p.toString(); } catch (Exception ex) { return "unknown"; }
        } catch (Exception ex) { return "system"; }
    }

    @PostMapping("/{entityName}/bulk")
    @Transactional
    public ResponseEntity<?> bulkCreate(@PathVariable("tenantId") UUID tenantId,
                                        @PathVariable("entityName") String entityName,
                                        @RequestBody java.util.List<Map<String, Object>> payloads) throws Exception {
        Optional<EntityDefinition> defOpt = apiGeneratorService.get(entityName);
        if (defOpt.isEmpty()) return ResponseEntity.notFound().build();
        EntityDefinition def = defOpt.get();

        if (!authorize("write", def)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        if (!isValidIdentifier(entityName) || !isValidIdentifier(def.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity name"));
        }

        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        String table = def.getName().toLowerCase();
        var cols = getTableColumns(schema, table);
        var fieldTypeMap = getFieldTypeMap(def);

        var ids = new java.util.ArrayList<UUID>();
        for (var payload : payloads) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            var insertCols = new java.util.ArrayList<String>();
            var insertParams = new java.util.ArrayList<Object>();
            insertCols.add("id"); insertParams.add(id);
            insertCols.add("tenant_id"); insertParams.add(tenantId);
            for (var e : payload.entrySet()) {
                String k = e.getKey();
                if (cols.contains(k)) {
                    String dtype = fieldTypeMap.getOrDefault(k, "STRING");
                    Object converted = convertValue(e.getValue(), dtype);
                    insertCols.add(k);
                    insertParams.add(converted);
                }
            }
            if (insertCols.size() == 2) {
                if (cols.contains("data")) {
                    insertCols.add("data");
                    insertParams.add(objectMapper.writeValueAsString(payload));
                } else {
                    throw new IllegalArgumentException("No matching columns in target table and no 'data' column present");
                }
            }
            String colList = String.join(", ", insertCols);
            String placeholders = String.join(", ", java.util.Collections.nCopies(insertCols.size(), "?"));
            String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, table, colList, placeholders);
            jdbcTemplate.update(sql, insertParams.toArray());
        }

        return ResponseEntity.status(201).body(Map.of("ids", ids));
    }

    @GetMapping("/{entityName}/{id}")
    public ResponseEntity<?> read(@PathVariable("tenantId") UUID tenantId,
                                  @PathVariable("entityName") String entityName,
                                  @PathVariable("id") UUID id) throws Exception {
        Optional<EntityDefinition> defOpt = apiGeneratorService.get(entityName);
        if (defOpt.isEmpty()) return ResponseEntity.notFound().build();
        EntityDefinition def = defOpt.get();

        if (!authorize("read", def)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        if (!isValidIdentifier(entityName) || !isValidIdentifier(def.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity name"));
        }

        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        String table = def.getName().toLowerCase();
        String sql = String.format("SELECT * FROM %s.%s WHERE id = ? AND tenant_id = ?", schema, table);

        Map<String, Object> row = jdbcTemplate.query(sql, new Object[]{id, tenantId}, rs -> {
            if (!rs.next()) return null;
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            java.sql.ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                String col = md.getColumnLabel(i);
                Object val = rs.getObject(i);
                m.put(col, val);
            }
            return m;
        });

        if (row == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(row);
    }

    @GetMapping("/{entityName}/list")
    public ResponseEntity<?> list(@PathVariable("tenantId") UUID tenantId,
                                  @PathVariable("entityName") String entityName,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "20") int size) throws Exception {
        Optional<EntityDefinition> defOpt = apiGeneratorService.get(entityName);
        if (defOpt.isEmpty()) return ResponseEntity.notFound().build();
        EntityDefinition def = defOpt.get();

        if (!authorize("read", def)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        if (!isValidIdentifier(entityName) || !isValidIdentifier(def.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity name"));
        }

        // Prefer using ApiGeneratorService dynamic query path when available.
        try {
                var rows = apiGeneratorService.listAll(tenantId, def);
            return ResponseEntity.ok(Map.of("page", page, "size", size, "items", rows));
        } catch (Exception ex) {
            // Fallback to direct JDBC paging for backwards compatibility
            String schema = "tenant_" + tenantId.toString().replace('-', '_');
            String table = def.getName().toLowerCase();

            String sql = String.format("SELECT * FROM %s.%s LIMIT ? OFFSET ?", schema, table);
            int offset = Math.max(0, page) * Math.max(1, size);

            var rows = jdbcTemplate.query(sql, new Object[]{size, offset}, rs -> {
                var list = new java.util.ArrayList<Map<String, Object>>();
                while (rs.next()) {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        String col = md.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        m.put(col, val);
                    }
                    list.add(m);
                }
                return list;
            });

            return ResponseEntity.ok(Map.of("page", page, "size", size, "items", rows));
        }
    }

    @PutMapping("/{entityName}/{id}")
    public ResponseEntity<?> update(@PathVariable("tenantId") UUID tenantId,
                                    @PathVariable("entityName") String entityName,
                                    @PathVariable("id") UUID id,
                                    @RequestBody Map<String, Object> payload) throws Exception {
        Optional<EntityDefinition> defOpt = apiGeneratorService.get(entityName);
        if (defOpt.isEmpty()) return ResponseEntity.notFound().build();
        EntityDefinition def = defOpt.get();

        if (!authorize("write", def)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        if (!isValidIdentifier(entityName) || !isValidIdentifier(def.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity name"));
        }

        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        String table = def.getName().toLowerCase();
        // determine columns present in table
        var cols = getTableColumns(schema, table);
        var updates = new java.util.ArrayList<String>();
        var params = new java.util.ArrayList<Object>();
        var fieldTypeMap = getFieldTypeMap(def);
        ResponseEntity<?> maybe = validatePayload(payload, def, false);
        if (maybe != null) return maybe;
        for (var e : payload.entrySet()) {
            String k = e.getKey();
            if (cols.contains(k) && !k.equalsIgnoreCase("id") && !k.equalsIgnoreCase("tenant_id")) {
                updates.add(k + " = ?");
                String dtype = fieldTypeMap.getOrDefault(k, "STRING");
                params.add(convertValue(e.getValue(), dtype));
            }
        }
        if (updates.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "no updatable columns in payload"));

        String sql = String.format("UPDATE %s.%s SET %s WHERE id = ? AND tenant_id = ?", schema, table, String.join(", ", updates));
        params.add(id);
        params.add(tenantId);
        jdbcTemplate.update(sql, params.toArray());
        try {
            com.wiseerp.audit.model.AuditLog entry = com.wiseerp.audit.model.AuditLog.builder()
                .tenantId(tenantId)
                .entityName(table)
                .entityId(id)
                .action("UPDATE")
                .payload(objectMapper.valueToTree(payload))
                .actor(principalName())
                .build();
            if (auditService != null) auditService.log(entry);
            if (eventPublisher != null) eventPublisher.publish(new com.wiseerp.events.DomainEvent(tenantId, table, id, "UPDATE", objectMapper.valueToTree(payload), java.time.Instant.now()));
        } catch (Exception ignored) {}

        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> validatePayload(Map<String, Object> payload, EntityDefinition def, boolean isCreate) {
        if (payload == null || def == null) return null;
        try {
            var fields = def.getFields();
            if (fields == null || !fields.isArray()) return null;
            for (var it = fields.elements(); it.hasNext(); ) {
                var f = it.next();
                String name = f.path("name").asText(null);
                boolean required = f.path("required").asBoolean(false);
                int maxLength = f.path("maxLength").asInt(-1);
                if (name == null) continue;
                Object v = payload.get(name);
                if (required && (v == null || (v instanceof String && ((String) v).isBlank()))) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Field '" + name + "' is required"));
                }
                if (maxLength > 0 && v instanceof String) {
                    if (((String) v).length() > maxLength) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Field '" + name + "' exceeds maxLength " + maxLength));
                    }
                }
            }
        } catch (Exception ex) {
            // if validation fails unexpectedly, don't block operation; log and continue
            // but when running in tests without logger available, swallow silently
        }
        return null;
    }

    @DeleteMapping("/{entityName}/{id}")
    public ResponseEntity<?> delete(@PathVariable("tenantId") UUID tenantId,
                                    @PathVariable("entityName") String entityName,
                                    @PathVariable("id") UUID id) throws Exception {
        Optional<EntityDefinition> defOpt = apiGeneratorService.get(entityName);
        if (defOpt.isEmpty()) return ResponseEntity.notFound().build();
        EntityDefinition def = defOpt.get();

        if (!authorize("delete", def)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        String table = def.getName().toLowerCase();
        String sql = String.format("DELETE FROM %s.%s WHERE id = ? AND tenant_id = ?", schema, table);
        int deleted = jdbcTemplate.update(sql, id, tenantId);
        if (deleted == 0) return ResponseEntity.notFound().build();
        try {
            com.wiseerp.audit.model.AuditLog entry = com.wiseerp.audit.model.AuditLog.builder()
                .tenantId(tenantId)
                .entityName(table)
                .entityId(id)
                .action("DELETE")
                .actor(principalName())
                .build();
            if (auditService != null) auditService.log(entry);
            if (eventPublisher != null) eventPublisher.publish(new com.wiseerp.events.DomainEvent(tenantId, table, id, "DELETE", null, java.time.Instant.now()));
        } catch (Exception ignored) {}
        return ResponseEntity.noContent().build();
    }

    private java.util.Set<String> getTableColumns(String schema, String table) throws Exception {
        try (var conn = jdbcTemplate.getDataSource().getConnection()) {
            var md = conn.getMetaData();
            try (var rs = md.getColumns(null, schema, table, "%")) {
                var s = new java.util.HashSet<String>();
                while (rs.next()) s.add(rs.getString("COLUMN_NAME"));
                return s;
            }
        }
    }

    private java.util.Map<String, String> getFieldTypeMap(EntityDefinition def) {
        var m = new java.util.HashMap<String, String>();
        try {
            var fields = def.getFields();
            if (fields != null && fields.isArray()) {
                for (var it = fields.elements(); it.hasNext(); ) {
                    var f = it.next();
                    String name = f.path("name").asText(null);
                    String dt = f.path("dataType").asText("STRING").toUpperCase();
                    if (name != null) m.put(name, dt);
                }
            }
        } catch (Exception ignored) {}
        return m;
    }

    private boolean isValidIdentifier(String raw) {
        if (raw == null) return false;
        String s = raw.toLowerCase(Locale.ROOT);
        return VALID_IDENTIFIER.matcher(s).matches();
    }

    private Object convertValue(Object raw, String dataType) {
        if (raw == null) return null;
        String dt = dataType == null ? "STRING" : dataType.toUpperCase();
        try {
            switch (dt) {
                case "INTEGER":
                    if (raw instanceof Number) return ((Number) raw).intValue();
                    return Integer.parseInt(raw.toString());
                case "DECIMAL":
                    if (raw instanceof BigDecimal) return raw;
                    return new BigDecimal(raw.toString());
                case "BOOLEAN":
                    if (raw instanceof Boolean) return raw;
                    return Boolean.parseBoolean(raw.toString());
                case "DATE":
                    if (raw instanceof Date) return raw;
                    try { return Date.valueOf(LocalDate.parse(raw.toString())); } catch (DateTimeParseException ex) { return Date.valueOf(raw.toString()); }
                case "DATETIME":
                    if (raw instanceof Timestamp) return raw;
                    try { return Timestamp.from(Instant.parse(raw.toString())); } catch (DateTimeParseException ex) { return Timestamp.valueOf(LocalDateTime.parse(raw.toString())); }
                case "JSON":
                    return objectMapper.writeValueAsString(raw);
                case "BINARY":
                    if (raw instanceof byte[]) return raw;
                    return java.util.Base64.getDecoder().decode(raw.toString());
                default:
                    return raw.toString();
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to convert value to " + dt + ": " + ex.getMessage(), ex);
        }
    }

    private boolean authorize(String action, EntityDefinition def) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return false;
            // Admin role always allowed
            if (auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"))) return true;
            // Per-entity role: ROLE_{ENTITY}_{ACTION}
            String ent = def != null && def.getName() != null ? def.getName().toUpperCase(Locale.ROOT) : "";
            String expected = "ROLE_" + ent + "_" + action.toUpperCase(Locale.ROOT);
            if (auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals(expected))) return true;

            // Delegate to AuthorizationService when available. Pass principal when it's a domain User,
            // otherwise the service implementation can inspect JWT claims itself (JwtAuthorizationService does).
            if (authorizationService != null) {
                com.wiseerp.iam.entity.User user = null;
                if (auth.getPrincipal() instanceof com.wiseerp.iam.entity.User) {
                    user = (com.wiseerp.iam.entity.User) auth.getPrincipal();
                }
                // Check per-entity/action role via service
                if (authorizationService.userHasRole(user, expected)) return true;
                // Check generic action-based role (e.g., ROLE_WRITE)
                if (authorizationService.userHasRole(user, action.toUpperCase(Locale.ROOT))) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
