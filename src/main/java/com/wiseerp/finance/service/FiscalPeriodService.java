package com.wiseerp.finance.service;

import com.wiseerp.finance.domain.FiscalPeriod;
import com.wiseerp.finance.repository.FiscalPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FiscalPeriodService {
    private final FiscalPeriodRepository fiscalPeriodRepository;

    public FiscalPeriod createFiscalPeriod(FiscalPeriod period, UUID tenantId) {
        if (period.getId() == null) {
            period.setId(UUID.randomUUID());
        }
        period.setTenantId(tenantId);
        return fiscalPeriodRepository.save(period);
    }
}
