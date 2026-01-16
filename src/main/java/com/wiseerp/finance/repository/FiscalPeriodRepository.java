package com.wiseerp.finance.repository;

import com.wiseerp.finance.domain.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, UUID> {
}
