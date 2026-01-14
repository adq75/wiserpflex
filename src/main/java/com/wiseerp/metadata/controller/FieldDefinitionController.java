package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.dto.FieldDefinitionDto;
import com.wiseerp.metadata.model.FieldDefinition;
import com.wiseerp.metadata.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/{tenantId}/metadata/entities/{entityId}/fields")
@Tag(name = "Metadata Fields", description = "Manage field definitions for entities")
public class FieldDefinitionController {

    private final MetadataService metadataService;

    public FieldDefinitionController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

        @Operation(summary = "Create field")
        @PostMapping
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Field definition payload",
            required = true,
            content = @Content(mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FieldDefinitionDto.class),
                examples = {@ExampleObject(name = "sku", value = "{\"name\":\"sku\",\"dataType\":\"STRING\"}")}
            )
        )
        public ResponseEntity<FieldDefinitionDto> createField(
            @Parameter(description = "Tenant ID") @PathVariable("tenantId") UUID tenantId,
            @Parameter(description = "Entity ID") @PathVariable("entityId") UUID entityId,
            @org.springframework.web.bind.annotation.RequestBody FieldDefinitionDto dto) {
        FieldDefinition f = toEntity(dto);
        f.setEntityDefinitionId(entityId);
        FieldDefinition saved = metadataService.createFieldDefinition(f);
        return ResponseEntity.created(URI.create("/api/" + tenantId + "/metadata/entities/" + entityId + "/fields/" + saved.getId()))
                .body(toDto(saved));
    }

    @Operation(summary = "List fields for entity")
    @GetMapping
    public ResponseEntity<List<FieldDefinitionDto>> listFields(@PathVariable("tenantId") UUID tenantId, @PathVariable("entityId") UUID entityId) {
        List<FieldDefinition> list = metadataService.listFieldsByEntity(entityId);
        List<FieldDefinitionDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get field by id")
    @GetMapping("/{id}")
    public ResponseEntity<FieldDefinitionDto> getField(@PathVariable("tenantId") UUID tenantId, @PathVariable("entityId") UUID entityId, @PathVariable("id") UUID id) {
        return metadataService.listFieldsByEntity(entityId).stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .map(f -> ResponseEntity.ok(toDto(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update field")
    @PutMapping("/{id}")
    public ResponseEntity<FieldDefinitionDto> updateField(@PathVariable("tenantId") UUID tenantId, @PathVariable("entityId") UUID entityId, @PathVariable("id") UUID id, @RequestBody FieldDefinitionDto dto) {
        FieldDefinition updated = toEntity(dto);
        FieldDefinition saved = metadataService.updateFieldDefinition(id, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @Operation(summary = "Delete field")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable("tenantId") UUID tenantId, @PathVariable("entityId") UUID entityId, @PathVariable("id") UUID id) {
        metadataService.deleteFieldDefinition(id);
        return ResponseEntity.noContent().build();
    }

    private FieldDefinition toEntity(FieldDefinitionDto dto) {
        return FieldDefinition.builder()
                .id(dto.getId())
                .entityDefinitionId(dto.getEntityDefinitionId())
                .name(dto.getName())
                .dataType(dto.getDataType())
                .isRequired(dto.getIsRequired())
                .defaultValue(dto.getDefaultValue())
                .validationRules(dto.getValidationRules())
                .uiConfig(dto.getUiConfig())
                .orderIndex(dto.getOrderIndex())
                .build();
    }

    private FieldDefinitionDto toDto(FieldDefinition e) {
        return FieldDefinitionDto.builder()
                .id(e.getId())
                .entityDefinitionId(e.getEntityDefinitionId())
                .name(e.getName())
                .dataType(e.getDataType())
                .isRequired(e.getIsRequired())
                .defaultValue(e.getDefaultValue())
                .validationRules(e.getValidationRules())
                .uiConfig(e.getUiConfig())
                .orderIndex(e.getOrderIndex())
                .build();
    }
}
