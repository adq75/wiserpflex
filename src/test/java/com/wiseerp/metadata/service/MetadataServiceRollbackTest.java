package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.MetadataVersion;
import com.wiseerp.metadata.repository.MetadataVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MetadataServiceRollbackTest {
    private MetadataVersionRepository versionRepo;
    private MetadataService service;

    @BeforeEach
    void setUp() {
        versionRepo = mock(MetadataVersionRepository.class);
        service = new MetadataService(null, null, null, versionRepo);
    }

    @Test
    void rollback_createsRollbackRecord() {
        UUID id = UUID.randomUUID();
        MetadataVersion existing = MetadataVersion.builder()
                .id(id)
                .entityDefinitionId(UUID.randomUUID())
                .version("v1")
                .build();

        when(versionRepo.findById(id)).thenReturn(Optional.of(existing));
        when(versionRepo.save(any(MetadataVersion.class))).thenAnswer(i -> i.getArgument(0));

        MetadataVersion rollback = service.rollbackVersion(id);
        assertThat(rollback.getId()).isNotNull().isNotEqualTo(id);
        assertThat(rollback.getVersion()).endsWith("-rollback");

        verify(versionRepo, times(1)).findById(id);
        verify(versionRepo, times(1)).save(rollback);
    }
}
