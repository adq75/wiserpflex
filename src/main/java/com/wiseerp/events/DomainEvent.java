package com.wiseerp.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent(UUID tenantId, String entityName, UUID entityId, String action, JsonNode payload, Instant timestamp) {}
