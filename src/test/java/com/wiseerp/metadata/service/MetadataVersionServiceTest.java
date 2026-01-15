package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.MetadataVersion;
import com.wiseerp.metadata.repository.MetadataVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MetadataVersionServiceTest {
    private MetadataVersionRepository versionRepo;
    private MetadataService service;

    @BeforeEach
    public void setUp() {
        versionRepo = mock(MetadataVersionRepository.class);
        // reuse other repos as mocks; not needed for this test
        service = new MetadataService(null, null, null, versionRepo);
    }

    @Test
    public void recordVersion_setsIdAndSaves() {
        UUID id = UUID.randomUUID();
        MetadataVersion input = MetadataVersion.builder()
                .id(null)
                .entityDefinitionId(UUID.randomUUID())
                .version("v1")
                .build();

        when(versionRepo.save(any(MetadataVersion.class))).thenAnswer(i -> {
            MetadataVersion v = i.getArgument(0);
            if (v.getId() == null) v.setId(id);
            return v;
        });

        MetadataVersion saved = service.recordVersion(input);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo("v1");
        verify(versionRepo, times(1)).save(saved);
    }
}
