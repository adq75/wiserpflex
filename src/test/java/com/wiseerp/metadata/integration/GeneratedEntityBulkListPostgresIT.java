package com.wiseerp.metadata.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.TestIntegrationApplication;
import com.wiseerp.metadata.dto.EntityDefinitionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestIntegrationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("it-postgres")
public class GeneratedEntityBulkListPostgresIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

        @Autowired
        DataSource dataSource;

        private UUID tenantId;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

        @Test
        void bulk_and_list_flow() throws Exception {
                tenantId = UUID.randomUUID();

        EntityDefinitionDto dto = EntityDefinitionDto.builder()
                .name("pg_entity_bulk")
                .displayName("PG Bulk Entity")
                .build();

        var fields = om.createArrayNode();
        var f = om.createObjectNode();
        f.put("name", "col_x");
        f.put("dataType", "STRING");
        fields.add(f);
        dto.setFields(fields);

        // apply
        mockMvc.perform(post("/api/system/metadata/entities/apply?tenantId=" + tenantId)
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // bulk create 5
        List<java.util.Map<String,Object>> payloads = new ArrayList<>();
        for (int i=0;i<5;i++){
            var n = om.createObjectNode();
            n.put("col_x","v"+i);
            payloads.add(om.convertValue(n, java.util.Map.class));
        }

        var bulk = mockMvc.perform(post("/api/"+tenantId+"/generated/pg_entity_bulk/bulk")
                        .with(jwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(payloads)))
                .andExpect(status().isCreated())
                .andReturn();

        var ids = om.readTree(bulk.getResponse().getContentAsString()).path("ids");
        assertThat(ids.size()).isEqualTo(5);

        // list page size 2 (use /list to avoid path-method conflict)
        var listRes = mockMvc.perform(get("/api/"+tenantId+"/generated/pg_entity_bulk/list?page=0&size=2").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var items = om.readTree(listRes.getResponse().getContentAsString()).path("items");
        assertThat(items.isArray()).isTrue();
        assertThat(items.size()).isEqualTo(2);
    }

        @org.junit.jupiter.api.AfterEach
        void cleanup() {
                if (tenantId == null) return;
                String schema = "tenant_" + tenantId.toString().replace('-', '_');
                try (var conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
                        st.executeUpdate("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
                } catch (Exception ignored) {}
                tenantId = null;
        }
}
