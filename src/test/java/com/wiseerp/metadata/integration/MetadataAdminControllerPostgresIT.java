package com.wiseerp.metadata.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.TestIntegrationApplication;
import com.wiseerp.metadata.dto.EntityDefinitionDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestIntegrationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("it-postgres")
public class MetadataAdminControllerPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    private final ObjectMapper om = new ObjectMapper();

    @AfterEach
    void cleanup() {
        try {
            jdbcTemplate.execute("DROP SCHEMA IF EXISTS tenant_test_schema CASCADE");
        } catch (Exception ignored) {
        }
    }

    @Test
    void preview_returns_ddl_and_apply_creates_table() throws Exception {
        UUID tenantId = UUID.randomUUID();

        EntityDefinitionDto dto = EntityDefinitionDto.builder()
                .name("pg_entity")
                .displayName("PG Entity")
                .build();

        var fields = om.createArrayNode();
        var f = om.createObjectNode();
        f.put("name", "col_x");
        f.put("dataType", "STRING");
        fields.add(f);
        dto.setFields(fields);

        // preview
        var previewResult = mockMvc.perform(post("/api/system/metadata/entities/preview?tenantId=" + tenantId)
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        String sql = previewResult.getResponse().getContentAsString();
        assertThat(sql).contains("tenant_").contains("pg_entity").contains("col_x");

        // apply
        var applyResult = mockMvc.perform(post("/api/system/metadata/entities/apply?tenantId=" + tenantId)
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        // verify table exists in Postgres
        DatabaseMetaData md = jdbcTemplate.getDataSource().getConnection().getMetaData();
        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        boolean found = false;
        try (ResultSet rs = md.getTables(null, schema, "%", null)) {
            while (rs.next()) {
                String tbl = rs.getString("TABLE_NAME");
                if (tbl != null && tbl.equalsIgnoreCase("pg_entity")) {
                    found = true;
                    break;
                }
            }
        }
        assertThat(found).isTrue();
    }
}
