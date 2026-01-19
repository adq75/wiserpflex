package com.wiseerp.metadata.integration;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.generator.ApiGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class GeneratedEntityAuditIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", postgres::getJdbcUrl);
        reg.add("spring.datasource.username", postgres::getUsername);
        reg.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JdbcTemplate jdbc;

    @MockBean
    ApiGeneratorService apiGeneratorService;

    UUID tenantId;

    @BeforeEach
    void setup() {
        tenantId = UUID.randomUUID();
        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        // create a simple table matching entity name 'widgets'
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".widgets (id UUID PRIMARY KEY, tenant_id UUID NOT NULL, name VARCHAR(200));");

        EntityDefinition def = EntityDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).name("widgets").build();
        when(apiGeneratorService.get("widgets")).thenReturn(Optional.of(def));
    }

    @Test
    void create_should_write_audit_entry() throws Exception {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("name", "mywidget");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> resp = rest.postForEntity("/api/" + tenantId + "/generated/widgets", req, Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        Map body = resp.getBody();
        assertThat(body).containsKey("id");

        // confirm row exists
        UUID id = UUID.fromString(body.get("id").toString());
        Integer count = jdbc.queryForObject("SELECT count(*) FROM " + "tenant_" + tenantId.toString().replace('-', '_') + ".widgets WHERE id = '" + id + "'", Integer.class);
        assertThat(count).isEqualTo(1);

        // confirm audit entry
        Integer audits = jdbc.queryForObject("SELECT count(*) FROM system.audit_log WHERE entity_id = '" + id + "'", Integer.class);
        assertThat(audits).isGreaterThanOrEqualTo(1);
    }
}
