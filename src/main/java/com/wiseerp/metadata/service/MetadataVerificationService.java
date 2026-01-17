package com.wiseerp.metadata.service;

import com.wiseerp.metadata.generator.ApiGeneratorService;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MetadataVerificationService {
    private final ApiGeneratorService apiGeneratorService;
    private final EntityDefinitionRepository entityDefinitionRepository;

    /**
     * Verify that an entity definition exists and is active for the given tenant.
     */
    public boolean verifyEntityForTenant(String entityName, UUID tenantId) {
        Optional<EntityDefinition> opt = apiGeneratorService.get(entityName);
        if (opt.isPresent()) {
            EntityDefinition def = opt.get();
            // Accept system-level templates (tenantId == null) as valid for any tenant
            if (!Boolean.TRUE.equals(def.getIsActive())) return false;
            if (def.getTenantId() == null) return true;
            return def.getTenantId().equals(tenantId);
        }
        // fallback: try repository lookup first, then scan as last resort
        try {
            // Prefer tenant-scoped lookup to avoid cross-tenant metadata leakage
            if (tenantId != null) {
                var tenantOpt = entityDefinitionRepository.findByNameIgnoreCaseAndTenantId(entityName, tenantId);
                if (tenantOpt != null && tenantOpt.isPresent()) {
                    var d = tenantOpt.get();
                    return Boolean.TRUE.equals(d.getIsActive()) && (d.getTenantId() == null || d.getTenantId().equals(tenantId));
                }
            }
            var repoOpt = entityDefinitionRepository.findByNameIgnoreCase(entityName);
            if (repoOpt != null && repoOpt.isPresent()) {
                var d = repoOpt.get();
                return Boolean.TRUE.equals(d.getIsActive()) && (d.getTenantId() == null || d.getTenantId().equals(tenantId));
            }
        } catch (Exception ignored) {
        }
        try {
            return entityDefinitionRepository.findAll().stream()
                    .filter(d -> d.getName() != null && d.getName().equalsIgnoreCase(entityName))
                    .anyMatch(d -> Boolean.TRUE.equals(d.getIsActive()) && (d.getTenantId() == null || d.getTenantId().equals(tenantId)));
        } catch (Exception ex) {
            return false;
        }
    }
}
