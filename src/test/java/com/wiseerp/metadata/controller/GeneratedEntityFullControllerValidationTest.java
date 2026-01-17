package com.wiseerp.metadata.controller;

import com.wiseerp.metadata.generator.ApiGeneratorService;
import com.wiseerp.metadata.model.EntityDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeneratedEntityFullControllerValidationTest {

    private ApiGeneratorService apiGeneratorService;
    private GeneratedEntityFullController controller;

    @BeforeEach
    void setup() {
        apiGeneratorService = mock(ApiGeneratorService.class);
        controller = new GeneratedEntityFullController(apiGeneratorService, null);
    }

    @Test
    void create_rejectsInvalidEntityName() throws Exception {
        UUID tenant = UUID.randomUUID();
        String badName = "Invalid-Name"; // contains hyphen -> invalid per validation

        EntityDefinition def = new EntityDefinition();
        def.setName("validname");

        when(apiGeneratorService.get(badName)).thenReturn(Optional.of(def));

        var resp = controller.create(tenant, badName, Map.of());
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void create_rejectsWhenDefinitionNameInvalid() throws Exception {
        UUID tenant = UUID.randomUUID();
        String goodName = "validname";

        EntityDefinition def = new EntityDefinition();
        def.setName("Invalid-Name");

        when(apiGeneratorService.get(goodName)).thenReturn(Optional.of(def));

        var resp = controller.create(tenant, goodName, Map.of());
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }
}
