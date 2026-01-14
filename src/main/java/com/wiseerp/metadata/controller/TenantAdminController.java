package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.dto.TenantCreateRequest;
import com.wiseerp.metadata.dto.TenantResponse;
import com.wiseerp.metadata.model.TenantEntity;
import com.wiseerp.metadata.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/system/tenants")
public class TenantAdminController {

    private final TenantService tenantService;

    public TenantAdminController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@RequestBody TenantCreateRequest req) {
        TenantEntity t = tenantService.createTenant(req.name());
        TenantResponse resp = new TenantResponse(t.getId(), t.getName(), t.getSchemaName(), t.getIsActive(), t.getCreatedAt());
        return ResponseEntity.created(URI.create("/api/system/tenants/" + t.getId())).body(resp);
    }
}
