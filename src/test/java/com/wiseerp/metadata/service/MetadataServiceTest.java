package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import com.wiseerp.metadata.repository.FieldDefinitionRepository;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import com.wiseerp.metadata.repository.MetadataVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MetadataServiceTest {

    private EntityDefinitionRepository entityRepo;
    private FieldDefinitionRepository fieldRepo;
    private MetadataChangeLogRepository changeLogRepo;
    private MetadataVersionRepository versionRepo;
    private MetadataService service;

    @BeforeEach
    public void setUp() {
        entityRepo = mock(EntityDefinitionRepository.class);
        fieldRepo = mock(FieldDefinitionRepository.class);
        changeLogRepo = mock(MetadataChangeLogRepository.class);
        versionRepo = mock(MetadataVersionRepository.class);
        service = new MetadataService(entityRepo, fieldRepo, changeLogRepo, versionRepo);
    }

    @Test
    public void createEntityDefinition_savesAndReturns() {
        EntityDefinition input = EntityDefinition.builder()
                .tenantId(UUID.randomUUID())
                .name("Product")
                .build();

        when(entityRepo.save(any(EntityDefinition.class))).thenAnswer(i -> i.getArgument(0));

        EntityDefinition result = service.createEntityDefinition(input);

        assertNotNull(result.getId());
        verify(entityRepo, times(1)).save(result);
    }
}
