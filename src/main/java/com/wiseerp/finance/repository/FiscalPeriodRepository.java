package com.wiseerp.finance.repository;

import com.wiseerp.finance.domain.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, UUID> {
	List<FiscalPeriod> findAllByTenantId(UUID tenantId);
	Optional<FiscalPeriod> findByIdAndTenantId(UUID id, UUID tenantId);
}
