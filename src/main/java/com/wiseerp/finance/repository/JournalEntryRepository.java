package com.wiseerp.finance.repository;

import com.wiseerp.finance.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findAllByTenantId(UUID tenantId);
    Optional<JournalEntry> findByIdAndTenantId(UUID id, UUID tenantId);
}
