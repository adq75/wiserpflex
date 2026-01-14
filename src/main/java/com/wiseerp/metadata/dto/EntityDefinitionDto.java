package com.wiseerp.metadata.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "EntityDefinition", description = "Metadata entity definition")
public class EntityDefinitionDto {
    @Schema(description = "Entity id")
    private UUID id;
    @Schema(description = "Tenant id")
    private UUID tenantId;
    @Schema(description = "Technical name")
    private String name;
    @Schema(description = "Display name")
    private String displayName;
    @Schema(description = "Category")
    private String category;
    @Schema(description = "Schema definition (JSON)")
    private JsonNode schemaDefinition;
    @Schema(description = "Fields definition (JSON)")
    private JsonNode fields;
    @Schema(description = "Version")
    private String version;
    @Schema(description = "Active flag")
    private Boolean isActive;
}
