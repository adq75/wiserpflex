package com.wiseerp.finance.service;

import com.wiseerp.finance.domain.FinancialReport;
import com.wiseerp.finance.repository.FinancialReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialReportService {
    private final FinancialReportRepository financialReportRepository;

    public FinancialReport createFinancialReport(FinancialReport report, UUID tenantId) {
        if (report.getId() == null) {
            report.setId(UUID.randomUUID());
        }
        report.setTenantId(tenantId);
        report.setGeneratedAt(Instant.now());
        return financialReportRepository.save(report);
    }
}
