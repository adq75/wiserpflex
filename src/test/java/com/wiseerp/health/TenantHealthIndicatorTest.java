package com.wiseerp.health;

import com.wiseerp.metadata.model.TenantEntity;
import com.wiseerp.metadata.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TenantHealthIndicatorTest {

    @Mock
    DataSource dataSource;
    @Mock
    Connection conn;
    @Mock
    PreparedStatement psSimple;
    @Mock
    PreparedStatement psInfo;
    @Mock
    ResultSet rsSimple;
    @Mock
    ResultSet rsInfo;
    @Mock
    TenantRepository tenantRepository;

    @BeforeEach
    void init() throws Exception {
        MockitoAnnotations.openMocks(this);
        Mockito.when(dataSource.getConnection()).thenReturn(conn);
    }

    @Test
    void healthUpWhenNoTenants() throws Exception {
        Mockito.when(conn.prepareStatement(Mockito.eq("SELECT 1"))).thenReturn(psSimple);
        Mockito.when(psSimple.executeQuery()).thenReturn(rsSimple);
        Mockito.when(rsSimple.next()).thenReturn(true);

        Mockito.when(tenantRepository.findAll()).thenReturn(List.of());

        TenantHealthIndicator indicator = new TenantHealthIndicator(dataSource, tenantRepository, List.of("audit_log"));
        Health h = indicator.health();
        assertEquals(Status.UP, h.getStatus());
    }

    @Test
    void healthDownWhenMissingTable() throws Exception {
        UUID id = UUID.randomUUID();
        TenantEntity t = new TenantEntity(id, "Acme", "tenant_" + id.toString().replace('-', '_'), true, Instant.now(), Instant.now(), null);

        Mockito.when(conn.prepareStatement(Mockito.eq("SELECT 1"))).thenReturn(psSimple);
        Mockito.when(psSimple.executeQuery()).thenReturn(rsSimple);
        Mockito.when(rsSimple.next()).thenReturn(true);

        Mockito.when(conn.prepareStatement(Mockito.contains("information_schema.tables"))).thenReturn(psInfo);
        Mockito.when(psInfo.executeQuery()).thenReturn(rsInfo);
        Mockito.when(rsInfo.next()).thenReturn(false); // table missing

        Mockito.when(tenantRepository.findAll()).thenReturn(List.of(t));

        TenantHealthIndicator indicator = new TenantHealthIndicator(dataSource, tenantRepository, List.of("audit_log"));
        Health h = indicator.health();
        assertEquals(Status.DOWN, h.getStatus());
    }
}
