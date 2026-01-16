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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.UUID;
import javax.sql.DataSource;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestIntegrationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("it-postgres")
public class GeneratedEntityE2EPostgresIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

        @Autowired
        DataSource dataSource;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

        private UUID tenantId;

        @AfterEach
        void cleanup() {
                if (tenantId == null) return;
                String schema = "tenant_" + tenantId.toString().replace('-', '_');
                try (var conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
                        // CASCADE to remove objects created during the test
                        st.executeUpdate("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
                } catch (Exception ignored) {
                }
                tenantId = null;
        }

    @Test
    void crud_flow_against_postgres() throws Exception {
        tenantId = UUID.randomUUID();

        EntityDefinitionDto dto = EntityDefinitionDto.builder()
                .name("pg_entity_e2e")
                .displayName("PG E2E Entity")
                .build();

        var fields = om.createArrayNode();
        var f = om.createObjectNode();
        f.put("name", "col_x");
        f.put("dataType", "STRING");
        fields.add(f);
        dto.setFields(fields);

        // apply entity definition (create table)
        mockMvc.perform(post("/api/system/metadata/entities/apply?tenantId=" + tenantId)
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // create a row
        var payload = om.createObjectNode();
        payload.put("col_x", "first");

        var createRes = mockMvc.perform(post("/api/" + tenantId + "/generated/pg_entity_e2e")
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();

        String id = om.readTree(createRes.getResponse().getContentAsString()).path("id").asText();
        assertThat(id).isNotEmpty();

        // read
        var readRes = mockMvc.perform(get("/api/" + tenantId + "/generated/pg_entity_e2e/" + id)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var node = om.readTree(readRes.getResponse().getContentAsString());
        assertThat(node.path("col_x").asText()).isEqualTo("first");

        // update
        var up = om.createObjectNode();
        up.put("col_x", "second");
        mockMvc.perform(put("/api/" + tenantId + "/generated/pg_entity_e2e/" + id)
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(up)))
                .andExpect(status().isOk());

        var read2 = mockMvc.perform(get("/api/" + tenantId + "/generated/pg_entity_e2e/" + id)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var node2 = om.readTree(read2.getResponse().getContentAsString());
        assertThat(node2.path("col_x").asText()).isEqualTo("second");

        // delete
        mockMvc.perform(delete("/api/" + tenantId + "/generated/pg_entity_e2e/" + id)
                        .with(jwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/" + tenantId + "/generated/pg_entity_e2e/" + id)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }
}
