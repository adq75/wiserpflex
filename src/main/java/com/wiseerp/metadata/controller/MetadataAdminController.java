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
    private final com.wiseerp.metadata.generator.DynamicEntityGenerator dynamicEntityGenerator;

    public MetadataAdminController(MetadataService metadataService, MetadataAuditService auditService,
                                   com.wiseerp.metadata.generator.DynamicEntityGenerator dynamicEntityGenerator) {
        this.metadataService = metadataService;
        this.auditService = auditService;
        this.dynamicEntityGenerator = dynamicEntityGenerator;
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

    @Operation(summary = "Preview CREATE TABLE DDL", description = "Generate the DDL for an entity definition without applying it (dry-run)")
    @ApiResponse(responseCode = "200", description = "DDL preview", content = @Content(mediaType = "text/plain"))
    @PostMapping("/entities/preview")
    public ResponseEntity<String> previewCreate(@RequestParam(value = "tenantId") UUID tenantId,
                                                @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entity definition", required = true) @RequestBody EntityDefinitionDto dto) {
        com.wiseerp.metadata.model.EntityDefinition def = dtoToModel(dto);
        String sql = dynamicEntityGenerator.buildCreateTableSql(tenantId, def);
        return ResponseEntity.ok(sql);
    }

    @Operation(summary = "Apply entity definition", description = "Create the table for the given entity definition and register it in metadata")
    @ApiResponse(responseCode = "201", description = "Entity created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityDefinitionDto.class)))
    @PostMapping("/entities/apply")
    public ResponseEntity<EntityDefinitionDto> applyCreate(@RequestParam(value = "tenantId") UUID tenantId,
                                                           @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entity definition", required = true) @RequestBody EntityDefinitionDto dto) {
        com.wiseerp.metadata.model.EntityDefinition model = dtoToModel(dto);
        if (model.getId() == null) model.setId(UUID.randomUUID());
        model.setTenantId(tenantId);
        com.wiseerp.metadata.model.EntityDefinition saved = metadataService.createEntityDefinition(model);
        // perform DDL creation (non-dry run)
        dynamicEntityGenerator.createTable(tenantId, saved, false);
        return ResponseEntity.created(URI.create("/api/system/metadata/entities/" + saved.getId())).body(toDto(saved));
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

    private EntityDefinition dtoToModel(EntityDefinitionDto d) {
        EntityDefinition m = EntityDefinition.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .name(d.getName())
                .displayName(d.getDisplayName())
                .category(d.getCategory())
                .schemaDefinition(d.getSchemaDefinition())
                .fields(d.getFields())
                .version(d.getVersion())
                .isActive(d.getIsActive())
                .build();
        return m;
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
