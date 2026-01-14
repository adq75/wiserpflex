package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.dto.EntityDefinitionDto;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.service.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
// removed invalid import alias; fully-qualified OpenAPI annotation used inline

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/{tenantId}/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

        @PostMapping("/entities")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Entity definition payload",
            required = true,
            content = @Content(mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = EntityDefinitionDto.class),
                examples = {@ExampleObject(name = "product", value = "{\"name\":\"Product\",\"displayName\":\"Product\",\"category\":\"MASTER\",\"version\":\"v1\"}")}
            )
        )
        public ResponseEntity<EntityDefinitionDto> createEntity(@PathVariable("tenantId") UUID tenantId,
                        @org.springframework.web.bind.annotation.RequestBody EntityDefinitionDto dto) {
        EntityDefinition def = toEntity(dto);
        def.setTenantId(tenantId);
        EntityDefinition saved = metadataService.createEntityDefinition(def);
        return ResponseEntity.created(URI.create("/api/" + tenantId + "/metadata/entities/" + saved.getId()))
                .body(toDto(saved));
    }

    @GetMapping("/entities")
    public ResponseEntity<List<EntityDefinitionDto>> listEntities(@PathVariable("tenantId") UUID tenantId) {
        List<EntityDefinition> list = metadataService.listByTenant(tenantId);
        List<EntityDefinitionDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/entities/{id}")
    public ResponseEntity<EntityDefinitionDto> getEntity(@PathVariable("tenantId") UUID tenantId, @PathVariable("id") UUID id) {
        return metadataService.getEntityDefinition(id)
                .filter(def -> tenantId.equals(def.getTenantId()))
                .map(def -> ResponseEntity.ok(toDto(def)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/entities/{id}")
    public ResponseEntity<EntityDefinitionDto> updateEntity(@PathVariable("tenantId") UUID tenantId,
                                                            @PathVariable("id") UUID id,
                                                            @RequestBody EntityDefinitionDto dto) {
        Optional<EntityDefinition> existing = metadataService.getEntityDefinition(id);
        if (existing.isEmpty() || !tenantId.equals(existing.get().getTenantId())) {
            return ResponseEntity.notFound().build();
        }
        EntityDefinition updated = toEntity(dto);
        updated.setTenantId(existing.get().getTenantId());
        EntityDefinition saved = metadataService.updateEntityDefinition(id, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/entities/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable("tenantId") UUID tenantId, @PathVariable("id") UUID id) {
        Optional<EntityDefinition> existing = metadataService.getEntityDefinition(id);
        if (existing.isEmpty() || !tenantId.equals(existing.get().getTenantId())) {
            return ResponseEntity.notFound().build();
        }
        metadataService.deleteEntityDefinition(id);
        return ResponseEntity.noContent().build();
    }

    private EntityDefinition toEntity(EntityDefinitionDto dto) {
        return EntityDefinition.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .name(dto.getName())
                .displayName(dto.getDisplayName())
                .category(dto.getCategory())
                .schemaDefinition(dto.getSchemaDefinition())
                .fields(dto.getFields())
                .version(dto.getVersion())
                .isActive(dto.getIsActive())
                .build();
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
}
