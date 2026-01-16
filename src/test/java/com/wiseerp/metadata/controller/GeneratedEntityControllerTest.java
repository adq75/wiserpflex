package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.generator.ApiGeneratorService;
import com.wiseerp.metadata.model.EntityDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeneratedEntityControllerTest {

    private ApiGeneratorService apiGeneratorService;
    private GeneratedEntityFullController controller;

    @BeforeEach
    void setup() {
        apiGeneratorService = mock(ApiGeneratorService.class);
        controller = new GeneratedEntityFullController(apiGeneratorService, null);
    }

    @Test
    public void listReturnsRows() throws Exception {
        UUID tenant = UUID.randomUUID();
        String entityName = "integ_entity";

        EntityDefinition def = new EntityDefinition();
        def.setName(entityName);

        when(apiGeneratorService.get(entityName)).thenReturn(Optional.of(def));
        when(apiGeneratorService.listAll(def)).thenReturn(List.of(Map.of("id", UUID.randomUUID(), "col_a", "v1")));

        var resp = controller.list(tenant, entityName, 0, 20);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
