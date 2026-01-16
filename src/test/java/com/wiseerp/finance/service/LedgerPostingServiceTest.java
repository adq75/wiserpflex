package com.wiseerp.finance.service;

import com.wiseerp.finance.domain.JournalEntry;
import com.wiseerp.finance.domain.JournalLine;
import com.wiseerp.finance.exception.LedgerPostingException;
import com.wiseerp.finance.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LedgerPostingServiceTest {

    private JournalEntryRepository journalEntryRepository;
    private LedgerPostingService ledgerPostingService;

    @BeforeEach
    void setup() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        ledgerPostingService = new LedgerPostingService(journalEntryRepository);
    }

    @Test
    void postBalancedJournalEntry_savesEntry() {
        UUID tenantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        JournalLine l1 = JournalLine.builder()
                .amount(new BigDecimal("100.00"))
                .accountId(accountId)
                .description("Debit")
                .build();

        JournalLine l2 = JournalLine.builder()
                .amount(new BigDecimal("-100.00"))
                .accountId(accountId)
                .description("Credit")
                .build();

        JournalEntry entry = JournalEntry.builder()
                .entryDate(Instant.now())
                .description("Test")
                .lines(List.of(l1, l2))
                .build();

        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JournalEntry saved = ledgerPostingService.postJournalEntry(tenantId, entry);

        assertNotNull(saved.getId());
        assertEquals(tenantId, saved.getTenantId());
        assertEquals(2, saved.getLines().size());
        verify(journalEntryRepository, times(1)).save(any());
    }

    @Test
    void postUnbalancedJournalEntry_throws() {
        UUID tenantId = UUID.randomUUID();

        JournalLine l1 = JournalLine.builder()
                .amount(new BigDecimal("100.00"))
                .build();

        JournalEntry entry = JournalEntry.builder()
                .entryDate(Instant.now())
                .lines(List.of(l1))
                .build();

        LedgerPostingException ex = assertThrows(LedgerPostingException.class,
                () -> ledgerPostingService.postJournalEntry(tenantId, entry));

        assertTrue(ex.getMessage().contains("does not balance"));
        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    void postNullEntry_throws() {
        UUID tenantId = UUID.randomUUID();
        assertThrows(LedgerPostingException.class, () -> ledgerPostingService.postJournalEntry(tenantId, null));
        verifyNoInteractions(journalEntryRepository);
    }
}
