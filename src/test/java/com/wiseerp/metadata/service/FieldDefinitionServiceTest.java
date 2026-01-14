package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.FieldDefinition;
import com.wiseerp.metadata.repository.FieldDefinitionRepository;
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
public class FieldDefinitionServiceTest {

    @Mock
    private FieldDefinitionRepository repo;

    @InjectMocks
    private FieldDefinitionService service;

    @Test
    void createAndList() {
        UUID entityId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        FieldDefinition f = FieldDefinition.builder().id(id).entityDefinitionId(entityId).name("sku").dataType("STRING").build();

        when(repo.save(f)).thenReturn(f);
        when(repo.findByEntityDefinitionId(entityId)).thenReturn(List.of(f));

        FieldDefinition saved = service.createField(f);
        assertThat(saved).isNotNull();
        assertThat(service.listFieldsByEntity(entityId)).hasSize(1).contains(f);
    }

    @Test
    void getField() {
        UUID id = UUID.randomUUID();
        FieldDefinition f = FieldDefinition.builder().id(id).name("a").build();
        when(repo.findById(id)).thenReturn(Optional.of(f));
        Optional<FieldDefinition> res = service.getField(id);
        assertThat(res).isPresent().contains(f);
    }
}
