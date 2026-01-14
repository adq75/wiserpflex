package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.dto.EntityDefinitionDto;
import com.wiseerp.metadata.dto.MetadataChangeLogDto;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.service.MetadataAuditService;
import com.wiseerp.metadata.service.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/entities")
    public ResponseEntity<List<EntityDefinitionDto>> listEntities(@RequestParam(value = "tenantId", required = false) UUID tenantId) {
        List<EntityDefinition> list = (tenantId == null) ? metadataService.listAllEntities() : metadataService.listByTenant(tenantId);
        List<EntityDefinitionDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/changes")
    public ResponseEntity<List<MetadataChangeLogDto>> listChanges(@RequestParam(value = "tenantId") UUID tenantId) {
        List<MetadataChangeLog> logs = auditService.listByTenant(tenantId);
        List<MetadataChangeLogDto> dtos = logs.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/changes")
    public ResponseEntity<MetadataChangeLogDto> createChange(@RequestBody MetadataChangeLogDto dto) {
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
