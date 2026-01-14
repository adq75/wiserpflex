package com.wiseerp.metadata.service;

import com.wiseerp.metadata.hooks.TenantSchemaCreator;
import com.wiseerp.metadata.model.TenantEntity;
import com.wiseerp.metadata.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final TenantSchemaCreator tenantSchemaCreator;

    public TenantService(TenantRepository tenantRepository, TenantSchemaCreator tenantSchemaCreator) {
        this.tenantRepository = tenantRepository;
        this.tenantSchemaCreator = tenantSchemaCreator;
    }

    @Transactional
    public TenantEntity createTenant(String name) {
        UUID id = UUID.randomUUID();
        String schemaName = "tenant_" + id.toString().replace('-', '_');

        TenantEntity tenant = TenantEntity.builder()
                .id(id)
                .name(name)
                .schemaName(schemaName)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TenantEntity saved = tenantRepository.save(tenant);

        // Hook: create schema and base tables for tenant
        tenantSchemaCreator.createSchemaFor(saved.getId());

        return saved;
    }
}
