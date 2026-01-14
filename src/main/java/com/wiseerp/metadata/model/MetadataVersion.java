package com.wiseerp.metadata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metadata_versions", schema = "system")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataVersion {

    @Id
    private UUID id;

    @Column(name = "entity_definition_id")
    private UUID entityDefinitionId;

    @Column(name = "version")
    private String version;

    @CreationTimestamp
    private Instant appliedAt;
}
