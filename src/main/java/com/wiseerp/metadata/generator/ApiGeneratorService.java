package com.wiseerp.metadata.generator;

import com.wiseerp.context.TenantContext;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.HashMap;

import com.wiseerp.metadata.query.DynamicQueryBuilder;
import com.wiseerp.metadata.query.DynamicQueryBuilder.Query;
import com.wiseerp.metadata.query.DynamicQueryExecutor;

@Service
@RequiredArgsConstructor
public class ApiGeneratorService {

    private final Map<String, EntityDefinition> registry = new ConcurrentHashMap<>();
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final DynamicQueryExecutor queryExecutor;
    private final Logger log = LoggerFactory.getLogger(ApiGeneratorService.class);

    @Value("${wiseerp.startup.auto-register.retry-count:5}")
    private int autoRegisterRetryCount = 5;

    @Value("${wiseerp.startup.auto-register.retry-delay-ms:1000}")
    private long autoRegisterRetryDelayMs = 1000L;

    @Value("${wiseerp.startup.auto-register.enabled:true}")
    private boolean autoRegisterEnabled = true;

    // status observability
    private volatile boolean lastAutoRegisterSuccess = false;
    private volatile long lastAutoRegisterAttempt = 0L;

    public void register(EntityDefinition def) {
        if (def == null || def.getName() == null) return;
        registry.put(def.getName().toLowerCase(), def);
    }

    public Optional<EntityDefinition> get(String name) {
        if (name == null) return Optional.empty();
        var r = registry.get(name.toLowerCase());
        if (r != null) return Optional.of(r);
        try {
            // Prefer tenant-scoped lookup when tenant context is present to avoid cross-tenant resolution
            String tenant = TenantContext.getTenantId();
            if (tenant != null) {
                try {
                    UUID tenantId = parseTenantString(tenant);
                    if (tenantId != null) {
                        var opt = entityDefinitionRepository.findByNameIgnoreCaseAndTenantId(name, tenantId);
                        if (opt != null && opt.isPresent()) return opt;
                    }
                } catch (Exception ignored) {
                    // fall through to non-tenant lookup
                }
            }
            return entityDefinitionRepository.findByNameIgnoreCase(name);
        } catch (Exception ex) {
            // fallback to full scan if repository method fails for any reason
            return entityDefinitionRepository.findAll().stream()
                    .filter(e -> e.getName() != null && e.getName().equalsIgnoreCase(name))
                    .findFirst();
        }
    }

    @PostConstruct
    public void initAutoRegister() {
        // auto-register entities from newly scaffolded modules so generator can discover them
        List<String> packagesToScan = List.of(
                "com.wiseerp.inventory",
                "com.wiseerp.sales",
                "com.wiseerp.purchase",
                "com.wiseerp.hr",
                "com.wiseerp.project",
                "com.wiseerp.assets"
        );

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(jakarta.persistence.Entity.class));

        // Respect feature flag
        if (!autoRegisterEnabled) {
            log.info("Auto-register disabled by configuration (wiseerp.startup.auto-register.enabled=false)");
            lastAutoRegisterSuccess = false;
            lastAutoRegisterAttempt = System.currentTimeMillis();
            return;
        }

        // Retry probe of metadata availability to wait for DB migrations to run
        int attempt = 0;
        boolean ready = false;
        while (attempt < autoRegisterRetryCount) {
            try {
                entityDefinitionRepository.count();
                ready = true;
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt >= autoRegisterRetryCount) {
                    log.warn("Skipping auto-register: metadata repository not available after {} attempts: {}", attempt, e.getMessage());
                    lastAutoRegisterSuccess = false;
                    lastAutoRegisterAttempt = System.currentTimeMillis();
                    return;
                }
                log.info("Metadata repository not ready (attempt {}/{}). Retrying in {}ms...", attempt, autoRegisterRetryCount, autoRegisterRetryDelayMs);
                try {
                    Thread.sleep(autoRegisterRetryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Auto-register probe interrupted");
                    lastAutoRegisterSuccess = false;
                    lastAutoRegisterAttempt = System.currentTimeMillis();
                    return;
                }
            }
        }
        // mark attempt time
        lastAutoRegisterAttempt = System.currentTimeMillis();

        for (String pkg : packagesToScan) {
            try {
                for (var bd : scanner.findCandidateComponents(pkg)) {
                    String className = bd.getBeanClassName();
                    try {
                        Class<?> cls = Class.forName(className);
                        String entityName = cls.getSimpleName();
                        // Prefer persisted metadata if available
                        EntityDefinition persisted = null;
                        try {
                            String tenant = TenantContext.getTenantId();
                            if (tenant != null) {
                                try {
                                    UUID tenantId = parseTenantString(tenant);
                                    if (tenantId != null) {
                                        var opt = entityDefinitionRepository.findByNameIgnoreCaseAndTenantId(entityName, tenantId);
                                        if (opt != null && opt.isPresent()) persisted = opt.get();
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            if (persisted == null) {
                                var opt = entityDefinitionRepository.findByNameIgnoreCase(entityName);
                                if (opt != null && opt.isPresent()) persisted = opt.get();
                            }
                        } catch (Exception ex) {
                            // fallback to full scan if repository method fails
                            try {
                                persisted = entityDefinitionRepository.findAll().stream()
                                        .filter(e -> e.getName() != null && e.getName().equalsIgnoreCase(entityName))
                                        .findFirst()
                                        .orElse(null);
                            } catch (Exception ignored) {
                            }
                        }

                        if (persisted != null) {
                            register(persisted);
                            log.info("Registered persisted EntityDefinition for: {}", entityName);
                        } else {
                            EntityDefinition ed = EntityDefinition.builder()
                                .name(entityName)
                                .displayName(entityName)
                                .version("v1")
                                .isActive(true)
                                .build();
                            register(ed);
                            log.info("Auto-registered transient entity for API generation: {}", entityName);
                        }
                    } catch (Exception ex) {
                        log.warn("Failed to load entity class {}: {}", className, ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("Scanning package {} failed: {}", pkg, ex.getMessage());
            }
        }
        lastAutoRegisterSuccess = true;
    }

    public boolean isAutoRegisterEnabled() {
        return autoRegisterEnabled;
    }

    public boolean getLastAutoRegisterSuccess() {
        return lastAutoRegisterSuccess;
    }

    public long getLastAutoRegisterAttempt() {
        return lastAutoRegisterAttempt;
    }

    /**
     * Build a parametrized SELECT SQL for the entity based on simple filters.
     * Returns SQL and ordered params. Caller must supply schema qualification when needed.
     */
    public Query buildSelectForEntity(EntityDefinition def, Map<String, Object> eqFilters, Map<String, Object> jsonbFilters) {
        String table = def.getName().toLowerCase();
        return DynamicQueryBuilder.buildSelect(table, eqFilters, jsonbFilters);
    }

    /**
     * Tenant-aware select builder: qualifies table with tenant schema and injects tenant_id filter.
     */
    public Query buildSelectForEntity(UUID tenantId, EntityDefinition def, Map<String, Object> eqFilters, Map<String, Object> jsonbFilters) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        if (def == null || def.getName() == null) throw new IllegalArgumentException("EntityDefinition is required");

        String schema = schemaName(tenantId);
        String table = schema + '.' + def.getName().toLowerCase();

        Map<String, Object> eqWithTenant = new HashMap<>();
        if (eqFilters != null) eqWithTenant.putAll(eqFilters);
        eqWithTenant.put("tenant_id", tenantId);

        if (jsonbFilters == null) jsonbFilters = Map.of();

        log.debug("Building tenant-scoped select for {}.{} with filters {} and jsonb {}", schema, def.getName(), eqWithTenant.keySet(), jsonbFilters.keySet());
        return DynamicQueryBuilder.buildSelect(table, eqWithTenant, jsonbFilters);
    }

    public List<Map<String, Object>> listAll(EntityDefinition def) {
        // If tenant context is present, prefer tenant-aware list
        String tenant = TenantContext.getTenantId();
        if (tenant != null) {
            try {
                String s = tenant;
                if (s.startsWith("tenant_")) s = s.substring(7);
                s = s.replace('_', '-');
                UUID tenantId = UUID.fromString(s);
                return listAll(tenantId, def);
            } catch (Exception ignored) {
                // fallback to non-tenant path
            }
        }
        var q = buildSelectForEntity(def, Map.of(), Map.of());
        return queryExecutor.execute(q);
    }

    public List<Map<String, Object>> listAll(UUID tenantId, EntityDefinition def) {
        var q = buildSelectForEntity(tenantId, def, Map.of(), Map.of());
        return queryExecutor.execute(q);
    }

    private String schemaName(UUID tenantId) {
        return "tenant_" + tenantId.toString().replace('-', '_');
    }

    private UUID parseTenantString(String tenant) {
        if (tenant == null) return null;
        String s = tenant;
        if (s.startsWith("tenant_")) s = s.substring(7);
        s = s.replace('_', '-');
        try {
            return UUID.fromString(s);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<String> listRegistered() {
        return registry.keySet().stream().sorted().collect(Collectors.toList());
    }
}
