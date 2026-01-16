package com.wiseerp;

import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import com.wiseerp.metadata.repository.FieldDefinitionRepository;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import com.wiseerp.metadata.repository.MetadataVersionRepository;
import com.wiseerp.metadata.repository.TenantRepository;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@ConditionalOnProperty(name = "test.use.mocks", havingValue = "true", matchIfMissing = false)
public class TestRepositoryConfig {

    @Bean
    @ConditionalOnMissingBean(TenantRepository.class)
    public TenantRepository tenantRepository() {
        return Mockito.mock(TenantRepository.class);
    }

    @Bean
    @ConditionalOnMissingBean(EntityDefinitionRepository.class)
    public EntityDefinitionRepository entityDefinitionRepository() {
        return Mockito.mock(EntityDefinitionRepository.class);
    }

    @Bean
    @ConditionalOnMissingBean(FieldDefinitionRepository.class)
    public FieldDefinitionRepository fieldDefinitionRepository() {
        return Mockito.mock(FieldDefinitionRepository.class);
    }

    @Bean
    @ConditionalOnMissingBean(MetadataVersionRepository.class)
    public MetadataVersionRepository metadataVersionRepository() {
        return Mockito.mock(MetadataVersionRepository.class);
    }

    // By default use mocks for repositories in unit tests. For integration tests
    // set -Dtest.use.mocks=false to let Spring create real Spring Data repositories
    @Bean
    @ConditionalOnMissingBean(MetadataChangeLogRepository.class)
    @ConditionalOnProperty(name = "test.use.mocks", havingValue = "true", matchIfMissing = true)
    public MetadataChangeLogRepository metadataChangeLogRepository() {
        return Mockito.mock(MetadataChangeLogRepository.class);
    }
}
