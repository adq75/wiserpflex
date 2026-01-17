package com.wiseerp.finance.repository;

import com.wiseerp.finance.domain.FinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, UUID> {
	List<FinancialReport> findAllByTenantId(UUID tenantId);
	Optional<FinancialReport> findByIdAndTenantId(UUID id, UUID tenantId);
}
