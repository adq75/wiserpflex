package com.wiseerp;

import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import com.wiseerp.metadata.repository.FieldDefinitionRepository;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import com.wiseerp.metadata.repository.MetadataVersionRepository;
import com.wiseerp.metadata.repository.TenantRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRepositoryConfig {

    @Bean
    public TenantRepository tenantRepository() {
        return Mockito.mock(TenantRepository.class);
    }

    @Bean
    public EntityDefinitionRepository entityDefinitionRepository() {
        return Mockito.mock(EntityDefinitionRepository.class);
    }

    @Bean
    public FieldDefinitionRepository fieldDefinitionRepository() {
        return Mockito.mock(FieldDefinitionRepository.class);
    }

    @Bean
    public MetadataVersionRepository metadataVersionRepository() {
        return Mockito.mock(MetadataVersionRepository.class);
    }

    @Bean
    public MetadataChangeLogRepository metadataChangeLogRepository() {
        return Mockito.mock(MetadataChangeLogRepository.class);
    }
}
