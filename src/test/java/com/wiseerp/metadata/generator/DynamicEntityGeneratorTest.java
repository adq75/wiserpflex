package com.wiseerp.metadata.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wiseerp.metadata.model.EntityDefinition;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicEntityGeneratorTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void buildCreateTableSql_contains_expected_columns() throws Exception {
        EntityDefinition def = EntityDefinition.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .name("TestEntity")
                .build();

        ArrayNode fields = om.createArrayNode();
        ObjectNode f1 = om.createObjectNode();
        f1.put("name", "col1");
        f1.put("dataType", "STRING");
        fields.add(f1);
        def.setFields(fields);

        DynamicEntityGenerator gen = new DynamicEntityGenerator(null, null);
        String sql = gen.buildCreateTableSql(def.getTenantId(), def);

        assertThat(sql).contains("tenant_");
        assertThat(sql).contains("col1");
        assertThat(sql).contains("tenant_id UUID NOT NULL");
    }
}
