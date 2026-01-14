package com.wiseerp.metadata.hooks;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

@Component
public class TenantSchemaCreator {
    private final DataSource defaultDataSource;

    public TenantSchemaCreator(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public void createSchemaFor(UUID tenantId) {
        String schemaName = sanitizeSchemaName(tenantId);
        try (Connection c = defaultDataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            s.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".audit_log (" +
                    "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                    "entity_name TEXT, change_type TEXT, payload JSONB, created_at TIMESTAMPTZ DEFAULT now())");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create schema for tenant " + tenantId, e);
        }
    }

    private String sanitizeSchemaName(UUID tenantId) {
        return "tenant_" + tenantId.toString().replace('-', '_');
    }
}
