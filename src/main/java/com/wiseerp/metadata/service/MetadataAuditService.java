package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MetadataAuditService {
    private final MetadataChangeLogRepository changeLogRepo;

    public MetadataAuditService(MetadataChangeLogRepository changeLogRepo) {
        this.changeLogRepo = changeLogRepo;
    }

    public MetadataChangeLog logChange(MetadataChangeLog log) {
        if (log.getId() == null) log.setId(UUID.randomUUID());
        return changeLogRepo.save(log);
    }

    public List<MetadataChangeLog> listByTenant(UUID tenantId) {
        return changeLogRepo.findByTenantId(tenantId);
    }
}
