package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetadataAuditServiceTest {

    @Mock
    private MetadataChangeLogRepository repo;

    @InjectMocks
    private MetadataAuditService service;

    @Test
    void logAndList() {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        MetadataChangeLog log = MetadataChangeLog.builder().id(id).tenantId(tenantId).changeType("CREATE").build();

        when(repo.save(log)).thenReturn(log);
        when(repo.findByTenantId(tenantId)).thenReturn(List.of(log));

        MetadataChangeLog saved = service.logChange(log);
        assertThat(saved).isNotNull();
        assertThat(service.listByTenant(tenantId)).hasSize(1).contains(log);
    }
}
