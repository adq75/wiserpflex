package com.wiseerp.finance.repository;

import com.wiseerp.finance.domain.FinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, UUID> {
}
