package com.wiseerp.metadata.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wiseerp.metadata.model.EntityDefinition;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DynamicEntityGeneratorValidationTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void validateDefinition_rejects_invalid_entity_name() {
        EntityDefinition def = EntityDefinition.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .name("1InvalidName")
                .build();

        DynamicEntityGenerator gen = new DynamicEntityGenerator(null, null);

        assertThatThrownBy(() -> gen.validateDefinition(def))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid entity name");
    }

    @Test
    void buildCreateTableSql_rejects_too_many_columns() {
        EntityDefinition def = EntityDefinition.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .name("validentity")
                .build();

        ArrayNode fields = om.createArrayNode();
        for (int i = 0; i < 205; i++) { // exceed MAX_COLUMNS (200)
            ObjectNode f = om.createObjectNode();
            f.put("name", "c" + i);
            f.put("dataType", "STRING");
            fields.add(f);
        }
        def.setFields(fields);

        DynamicEntityGenerator gen = new DynamicEntityGenerator(null, null);

        assertThatThrownBy(() -> gen.buildCreateTableSql(def.getTenantId(), def))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many columns");
    }
}
