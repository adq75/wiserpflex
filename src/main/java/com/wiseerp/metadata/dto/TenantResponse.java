package com.wiseerp.metadata.dto;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(UUID id, String name, String schemaName, Boolean isActive, Instant createdAt) {
}
