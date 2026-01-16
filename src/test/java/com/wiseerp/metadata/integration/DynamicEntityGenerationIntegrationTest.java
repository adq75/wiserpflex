package com.wiseerp.metadata.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.metadata.generator.DynamicEntityGenerator;
import com.wiseerp.metadata.model.EntityDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.wiseerp.TestIntegrationApplication.class)
public class DynamicEntityGenerationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DynamicEntityGenerator generator;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void create_table_for_tenant_and_verify_exists() throws Exception {
        UUID tenantId = UUID.randomUUID();
        EntityDefinition def = EntityDefinition.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name("integ_entity")
                .build();

        // simple single field
        var fields = om.createArrayNode();
        var f = om.createObjectNode();
        f.put("name", "col_a");
        f.put("dataType", "STRING");
        fields.add(f);
        def.setFields(fields);

        generator.createTable(tenantId, def, false);

        DatabaseMetaData md = jdbcTemplate.getDataSource().getConnection().getMetaData();
        String schema = "tenant_" + tenantId.toString().replace('-', '_');
        try (ResultSet rs = md.getTables(null, null, "%", null)) {
            boolean found = false;
            while (rs.next()) {
                String tbl = rs.getString("TABLE_NAME");
                String tblSchema = rs.getString("TABLE_SCHEM");
                if (tbl != null && tbl.equalsIgnoreCase("integ_entity") && tblSchema != null && tblSchema.equalsIgnoreCase(schema)) {
                    found = true;
                    break;
                }
            }
            assertThat(found).isTrue();
        }
    }
}
