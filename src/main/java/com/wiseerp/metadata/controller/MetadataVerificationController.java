package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.service.MetadataVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MetadataVerificationController {
    private final MetadataVerificationService verificationService;

        @GetMapping("/api/{tenantId}/metadata/verify")
        public ResponseEntity<Map<String, Object>> verifyEntity(
            @PathVariable("tenantId") UUID tenantId,
            @RequestParam("entityName") String entityName) {

        boolean exists = verificationService.verifyEntityForTenant(entityName, tenantId);
        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "entityName", entityName,
                "exists", exists
        ));
    }
}
