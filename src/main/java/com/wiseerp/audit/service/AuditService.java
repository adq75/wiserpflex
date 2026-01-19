package com.wiseerp.audit.service;

import com.wiseerp.audit.model.AuditLog;
import com.wiseerp.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditService {
    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public AuditLog log(AuditLog entry) {
        if (entry.getId() == null) entry.setId(UUID.randomUUID());
        return repo.save(entry);
    }

    public List<AuditLog> listByTenant(UUID tenantId) {
        return repo.findByTenantId(tenantId);
    }
}
