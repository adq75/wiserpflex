package com.wiseerp.metadata.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metadata_change_log", schema = "system")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataChangeLog {

    @Id
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "entity_definition_id")
    private UUID entityDefinitionId;

    @Column(name = "change_type")
    private String changeType;

    @Type(JsonBinaryType.class)
    @Column(name = "change_payload", columnDefinition = "jsonb")
    private JsonNode changePayload;

    @Column(name = "changed_by")
    private String changedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
