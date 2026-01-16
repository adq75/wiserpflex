package com.wiseerp.finance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fiscal_periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalPeriod {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean closed;
}
