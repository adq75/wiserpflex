package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntityRegistryServiceTest {

    @Mock
    private EntityDefinitionRepository repo;

    @InjectMocks
    private EntityRegistryService service;

    @Test
    void createAndListByTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        EntityDefinition e = EntityDefinition.builder().id(id).tenantId(tenantId).name("X").build();

        when(repo.save(e)).thenReturn(e);
        when(repo.findByTenantId(tenantId)).thenReturn(List.of(e));

        EntityDefinition saved = service.createEntity(e);
        assertThat(saved).isNotNull();
        assertThat(service.listByTenant(tenantId)).hasSize(1).contains(e);
    }

    @Test
    void getEntity() {
        UUID id = UUID.randomUUID();
        EntityDefinition e = EntityDefinition.builder().id(id).name("Z").build();
        when(repo.findById(id)).thenReturn(Optional.of(e));
        Optional<EntityDefinition> res = service.getEntity(id);
        assertThat(res).isPresent().contains(e);
    }
}
