package com.wiseerp.finance.service;

import com.wiseerp.finance.domain.JournalEntry;
import com.wiseerp.finance.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JournalEntryService {
    private final JournalEntryRepository journalEntryRepository;

    public JournalEntry createJournalEntry(JournalEntry entry, UUID tenantId) {
        if (entry.getId() == null) {
            entry.setId(UUID.randomUUID());
        }
        entry.setTenantId(tenantId);
        return journalEntryRepository.save(entry);
    }
}
