package com.wiseerp.finance.service;

import com.wiseerp.finance.domain.JournalEntry;
import com.wiseerp.finance.domain.JournalLine;
import com.wiseerp.finance.exception.LedgerPostingException;
import com.wiseerp.finance.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerPostingService {
    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public JournalEntry postJournalEntry(UUID tenantId, JournalEntry entry) {
        if (entry == null) throw new LedgerPostingException("JournalEntry cannot be null");
        if (entry.getId() == null) entry.setId(UUID.randomUUID());
        entry.setTenantId(tenantId);

        List<JournalLine> lines = entry.getLines();
        if (lines == null || lines.isEmpty()) {
            throw new LedgerPostingException("Journal entry must contain at least one line");
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (JournalLine line : lines) {
            if (line == null) throw new LedgerPostingException("Journal line cannot be null");
            if (line.getId() == null) line.setId(UUID.randomUUID());
            line.setTenantId(tenantId);
            line.setJournalEntryId(entry.getId());
            if (line.getAmount() == null) throw new LedgerPostingException("Journal line amount is required");
            sum = sum.add(line.getAmount());
        }

        if (sum.compareTo(BigDecimal.ZERO) != 0) {
            throw new LedgerPostingException("Journal entry does not balance; sum=" + sum);
        }

        return journalEntryRepository.save(entry);
    }
}
