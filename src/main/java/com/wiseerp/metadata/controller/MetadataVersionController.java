package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.dto.MetadataVersionDto;
import com.wiseerp.metadata.model.MetadataVersion;
import com.wiseerp.metadata.service.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/system/metadata/versions")
public class MetadataVersionController {
    private final MetadataService metadataService;

    public MetadataVersionController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping
    public ResponseEntity<MetadataVersionDto> recordVersion(@RequestBody MetadataVersionDto dto) {
        MetadataVersion model = MetadataVersion.builder()
                .id(dto.getId())
                .entityDefinitionId(dto.getEntityDefinitionId())
                .version(dto.getVersion())
                .build();
        MetadataVersion saved = metadataService.recordVersion(model);
        MetadataVersionDto out = MetadataVersionDto.builder()
                .id(saved.getId())
                .entityDefinitionId(saved.getEntityDefinitionId())
                .version(saved.getVersion())
                .appliedAt(saved.getAppliedAt())
                .build();
        return ResponseEntity.created(URI.create("/api/system/metadata/versions/" + saved.getId())).body(out);
    }
}
