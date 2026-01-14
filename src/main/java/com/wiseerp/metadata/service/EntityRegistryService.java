package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EntityRegistryService {
    private final EntityDefinitionRepository entityRepo;

    public EntityRegistryService(EntityDefinitionRepository entityRepo) {
        this.entityRepo = entityRepo;
    }

    public EntityDefinition createEntity(EntityDefinition def) {
        if (def.getId() == null) def.setId(UUID.randomUUID());
        return entityRepo.save(def);
    }

    public Optional<EntityDefinition> getEntity(UUID id) {
        return entityRepo.findById(id);
    }

    public List<EntityDefinition> listByTenant(UUID tenantId) {
        return entityRepo.findByTenantId(tenantId);
    }

    public void deleteEntity(UUID id) {
        entityRepo.deleteById(id);
    }
}
