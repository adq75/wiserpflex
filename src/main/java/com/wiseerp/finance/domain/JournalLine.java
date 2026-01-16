package com.wiseerp.finance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "journal_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalLine {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "journal_entry_id", columnDefinition = "uuid")
    private UUID journalEntryId;

    @Column(name = "account_id", columnDefinition = "uuid")
    private UUID accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;
}
