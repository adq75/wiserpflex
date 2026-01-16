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
@Table(name = "field_definitions", schema = "system")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDefinition {

    @Id
    private UUID id;

    @Column(name = "entity_definition_id", nullable = false)
    private UUID entityDefinitionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "default_value")
    private String defaultValue;

    @Type(JsonBinaryType.class)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private JsonNode validationRules;

    @Type(JsonBinaryType.class)
    @Column(name = "ui_config", columnDefinition = "jsonb")
    private JsonNode uiConfig;

    @Column(name = "order_index")
    private Integer orderIndex;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
