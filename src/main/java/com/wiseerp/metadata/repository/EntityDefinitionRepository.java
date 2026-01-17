package com.wiseerp.metadata.repository;

import com.wiseerp.metadata.model.EntityDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityDefinitionRepository extends JpaRepository<EntityDefinition, UUID> {
    List<EntityDefinition> findByTenantId(UUID tenantId);
    Optional<EntityDefinition> findByNameIgnoreCase(String name);
    Optional<EntityDefinition> findByNameIgnoreCaseAndTenantId(String name, UUID tenantId);
}
