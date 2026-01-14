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
@Schema(name = "FieldDefinition", description = "Definition of a single field")
public class FieldDefinitionDto {
    @Schema(description = "Field id")
    private UUID id;
    @Schema(description = "Parent entity id")
    private UUID entityDefinitionId;
    @Schema(description = "Field name")
    private String name;
    @Schema(description = "Data type")
    private String dataType;
    @Schema(description = "Required flag")
    private Boolean isRequired;
    @Schema(description = "Default value")
    private String defaultValue;
    @Schema(description = "Validation rules (JSON)")
    private JsonNode validationRules;
    @Schema(description = "UI config (JSON)")
    private JsonNode uiConfig;
    @Schema(description = "Order index")
    private Integer orderIndex;
}
