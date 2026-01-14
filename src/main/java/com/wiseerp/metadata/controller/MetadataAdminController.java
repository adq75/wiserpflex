package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.dto.EntityDefinitionDto;
import com.wiseerp.metadata.dto.MetadataChangeLogDto;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.service.MetadataAuditService;
import com.wiseerp.metadata.service.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/metadata")
public class MetadataAdminController {

    private final MetadataService metadataService;
    private final MetadataAuditService auditService;

    public MetadataAdminController(MetadataService metadataService, MetadataAuditService auditService) {
        this.metadataService = metadataService;
        this.auditService = auditService;
    }

    @Operation(summary = "List entity definitions", description = "List all entity definitions or filter by tenantId")
    @ApiResponse(responseCode = "200", description = "List returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityDefinitionDto.class)))
    @GetMapping("/entities")
    public ResponseEntity<List<EntityDefinitionDto>> listEntities(@Parameter(description = "Optional tenant id to filter by") @RequestParam(value = "tenantId", required = false) UUID tenantId) {
        List<EntityDefinition> list = (tenantId == null) ? metadataService.listAllEntities() : metadataService.listByTenant(tenantId);
        List<EntityDefinitionDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "List metadata change logs", description = "List metadata change log entries for a tenant")
    @ApiResponse(responseCode = "200", description = "Change logs returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetadataChangeLogDto.class)))
    @GetMapping("/changes")
    public ResponseEntity<List<MetadataChangeLogDto>> listChanges(@Parameter(description = "Tenant id") @RequestParam(value = "tenantId") UUID tenantId) {
        List<MetadataChangeLog> logs = auditService.listByTenant(tenantId);
        List<MetadataChangeLogDto> dtos = logs.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Create metadata change log", description = "Record a metadata change for auditing")
    @ApiResponse(responseCode = "201", description = "Change logged", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetadataChangeLogDto.class)))
    @PostMapping("/changes")
    public ResponseEntity<MetadataChangeLogDto> createChange(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Change payload", required = true, content = @Content(schema = @Schema(implementation = MetadataChangeLogDto.class))) @RequestBody MetadataChangeLogDto dto) {
        MetadataChangeLog model = toModel(dto);
        MetadataChangeLog saved = auditService.logChange(model);
        return ResponseEntity.created(URI.create("/api/system/metadata/changes/" + saved.getId())).body(toDto(saved));
    }

    private EntityDefinitionDto toDto(EntityDefinition e) {
        return EntityDefinitionDto.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .displayName(e.getDisplayName())
                .category(e.getCategory())
                .schemaDefinition(e.getSchemaDefinition())
                .fields(e.getFields())
                .version(e.getVersion())
                .isActive(e.getIsActive())
                .build();
    }

    private MetadataChangeLogDto toDto(MetadataChangeLog l) {
        return MetadataChangeLogDto.builder()
                .id(l.getId())
                .tenantId(l.getTenantId())
                .entityDefinitionId(l.getEntityDefinitionId())
                .changeType(l.getChangeType())
                .changePayload(l.getChangePayload())
                .changedBy(l.getChangedBy())
                .createdAt(l.getCreatedAt())
                .build();
    }

    private MetadataChangeLog toModel(MetadataChangeLogDto d) {
        return MetadataChangeLog.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .entityDefinitionId(d.getEntityDefinitionId())
                .changeType(d.getChangeType())
                .changePayload(d.getChangePayload())
                .changedBy(d.getChangedBy())
                .build();
    }
}
