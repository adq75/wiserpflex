package com.wiseerp.metadata.repository;

import com.wiseerp.metadata.model.MetadataChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MetadataChangeLogRepository extends JpaRepository<MetadataChangeLog, UUID> {
    List<MetadataChangeLog> findByTenantId(UUID tenantId);
}
