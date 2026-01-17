package com.wiseerp.metadata.generator;

import com.wiseerp.context.TenantContext;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import com.wiseerp.metadata.query.DynamicQueryExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiGeneratorServiceTenantLookupTest {

    private EntityDefinitionRepository repo;
    private DynamicQueryExecutor queryExecutor;

    @BeforeEach
    void setup() {
        repo = mock(EntityDefinitionRepository.class);
        queryExecutor = mock(DynamicQueryExecutor.class);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void get_prefersTenantScopedLookupWhenTenantContextPresent() {
        UUID tenant = UUID.randomUUID();
        String tenantStr = "tenant_" + tenant.toString().replace('-', '_');

        TenantContext.setTenantId(tenantStr);

        EntityDefinition expected = new EntityDefinition();
        expected.setName("MyEntity");
        expected.setTenantId(tenant);

        when(repo.findByNameIgnoreCaseAndTenantId("MyEntity", tenant)).thenReturn(Optional.of(expected));

        ApiGeneratorService svc = new ApiGeneratorService(repo, queryExecutor);

        var res = svc.get("MyEntity");
        assertThat(res).isPresent().contains(expected);
    }
}
