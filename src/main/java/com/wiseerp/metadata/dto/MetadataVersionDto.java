package com.wiseerp.metadata.dto;

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
@Schema(name = "MetadataVersion", description = "Metadata version record")
public class MetadataVersionDto {
    @Schema(description = "Version id")
    private UUID id;
    @Schema(description = "Entity definition id")
    private UUID entityDefinitionId;
    @Schema(description = "Version string")
    private String version;
    @Schema(description = "Applied at timestamp")
    private Instant appliedAt;
}
