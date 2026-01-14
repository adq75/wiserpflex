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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entity_definitions", schema = "system")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityDefinition {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column
    private String category;

    @Type(JsonBinaryType.class)
    @Column(name = "schema_definition", columnDefinition = "jsonb")
    private JsonNode schemaDefinition;

    @Type(JsonBinaryType.class)
    @Column(name = "fields", columnDefinition = "jsonb")
    private JsonNode fields;

    @Column
    private String version;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
