package com.wiseerp.metadata.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "MetadataChangeLog", description = "Metadata change log entry")
public class MetadataChangeLogDto {
    @Schema(description = "Change id")
    private UUID id;
    @Schema(description = "Tenant id")
    private UUID tenantId;
    @Schema(description = "Entity definition id")
    private UUID entityDefinitionId;
    @Schema(description = "Change type (CREATE|UPDATE|DELETE)")
    private String changeType;
    @Schema(description = "Change payload")
    private JsonNode changePayload;
    @Schema(description = "User who changed")
    private String changedBy;
    @Schema(description = "Created at")
    private Instant createdAt;
}
