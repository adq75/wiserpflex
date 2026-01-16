package com.wiseerp.finance.controller;

import com.wiseerp.finance.domain.Account;
import com.wiseerp.finance.domain.FiscalPeriod;
import com.wiseerp.finance.domain.FinancialReport;
import com.wiseerp.finance.domain.JournalEntry;
import com.wiseerp.finance.service.AccountService;
import com.wiseerp.finance.service.JournalEntryService;
import com.wiseerp.finance.service.FiscalPeriodService;
import com.wiseerp.finance.service.FinancialReportService;
import com.wiseerp.finance.service.LedgerPostingService;
import com.wiseerp.metadata.service.MetadataVerificationService;
import com.wiseerp.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/{tenantId}/finance")
@RequiredArgsConstructor
public class FinancialController {
    private final AccountService accountService;
    private final JournalEntryService journalEntryService;
    private final FiscalPeriodService fiscalPeriodService;
    private final FinancialReportService financialReportService;
    private final MetadataVerificationService metadataVerificationService;
    private final LedgerPostingService ledgerPostingService;

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@PathVariable("tenantId") UUID tenantId, @RequestBody Account account) {
        boolean ok = metadataVerificationService.verifyEntityForTenant("Account", tenantId);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
        }
        Account created = accountService.createAccount(account, tenantId);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/journal-entries")
    public ResponseEntity<JournalEntry> createJournalEntry(@PathVariable("tenantId") UUID tenantId, @RequestBody JournalEntry entry) {
        boolean ok = metadataVerificationService.verifyEntityForTenant("JournalEntry", tenantId);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
        }
        try {
            // Ensure tenant context is set for routing datasource (schema-per-tenant)
            TenantContext.setTenantId("tenant_" + tenantId.toString());
            JournalEntry posted = ledgerPostingService.postJournalEntry(tenantId, entry);
            return ResponseEntity.status(HttpStatus.CREATED).body(posted);
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/fiscal-periods")
    public ResponseEntity<FiscalPeriod> createFiscalPeriod(@PathVariable("tenantId") UUID tenantId, @RequestBody FiscalPeriod period) {
        boolean ok = metadataVerificationService.verifyEntityForTenant("FiscalPeriod", tenantId);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
        }
        FiscalPeriod created = fiscalPeriodService.createFiscalPeriod(period, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/reports")
    public ResponseEntity<FinancialReport> createFinancialReport(@PathVariable("tenantId") UUID tenantId, @RequestBody FinancialReport report) {
        boolean ok = metadataVerificationService.verifyEntityForTenant("FinancialReport", tenantId);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
        }
        FinancialReport created = financialReportService.createFinancialReport(report, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
