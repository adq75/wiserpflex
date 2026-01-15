package com.wiseerp.health;

import com.wiseerp.metadata.model.TenantEntity;
import com.wiseerp.metadata.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@Component("tenant")
public class TenantHealthIndicator implements HealthIndicator {
    private final DataSource defaultDataSource;
    private final TenantRepository tenantRepository;
    private final List<String> requiredTables;

    @Autowired
    public TenantHealthIndicator(@Qualifier("defaultDataSource") DataSource defaultDataSource,
                                 TenantRepository tenantRepository,
                                 @Value("${wiseerp.health.required-tables:}") String requiredTablesRaw) {
        this.defaultDataSource = defaultDataSource;
        this.tenantRepository = tenantRepository;
        if (requiredTablesRaw == null || requiredTablesRaw.isBlank()) {
            this.requiredTables = java.util.List.of();
        } else {
            this.requiredTables = java.util.Arrays.asList(requiredTablesRaw.split(","));
        }
    }

    // Constructor overload used by unit tests to pass a List<String> directly
    public TenantHealthIndicator(DataSource defaultDataSource,
                                 TenantRepository tenantRepository,
                                 List<String> requiredTables) {
        this.defaultDataSource = defaultDataSource;
        this.tenantRepository = tenantRepository;
        this.requiredTables = (requiredTables == null) ? java.util.List.of() : requiredTables;
    }

    @Override
    public Health health() {
        try (Connection c = defaultDataSource.getConnection()) {
            // basic connectivity
            try (PreparedStatement p = c.prepareStatement("SELECT 1")) {
                try (ResultSet r = p.executeQuery()) {
                    // ok
                }
            }

            List<TenantEntity> tenants = tenantRepository.findAll();
            for (TenantEntity t : tenants) {
                if (!Boolean.TRUE.equals(t.getIsActive())) continue;
                String schema = t.getSchemaName();
                for (String table : requiredTables) {
                    try (PreparedStatement p = c.prepareStatement(
                            "SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ? LIMIT 1")) {
                        p.setString(1, schema);
                        p.setString(2, table);
                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) {
                                return Health.down()
                                        .withDetail("missing_table", String.format("%s.%s", schema, table))
                                        .withDetail("tenant", t.getId())
                                        .build();
                            }
                        }
                    }
                }
            }

            return Health.up().withDetail("tenants_checked", tenants.size()).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
