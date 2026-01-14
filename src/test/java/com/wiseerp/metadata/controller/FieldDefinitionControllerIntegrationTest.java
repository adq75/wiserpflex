package com.wiseerp.metadata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.metadata.dto.FieldDefinitionDto;
import com.wiseerp.metadata.model.EntityDefinition;
import java.util.Optional;
import com.wiseerp.metadata.model.FieldDefinition;
import com.wiseerp.metadata.service.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.wiseerp.TestApplication.class)
@AutoConfigureMockMvc
public class FieldDefinitionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetadataService metadataService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UUID tenantId;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        entityId = UUID.randomUUID();
    }

    @Test
    void createAndListField() throws Exception {
        UUID id = UUID.randomUUID();
        FieldDefinition saved = FieldDefinition.builder()
                .id(id)
                .entityDefinitionId(entityId)
                .name("sku")
                .dataType("STRING")
                .build();

        when(metadataService.createFieldDefinition(any(FieldDefinition.class))).thenReturn(saved);
        when(metadataService.listFieldsByEntity(entityId)).thenReturn(List.of(saved));
        EntityDefinition ent = EntityDefinition.builder()
            .id(entityId)
            .tenantId(tenantId)
            .name("TestEntity")
            .build();
        when(metadataService.getEntityDefinition(entityId)).thenReturn(Optional.of(ent));

        FieldDefinitionDto req = FieldDefinitionDto.builder().name("sku").dataType("STRING").build();

        mockMvc.perform(post("/api/" + tenantId + "/metadata/entities/" + entityId + "/fields")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("sku"));

        mockMvc.perform(get("/api/" + tenantId + "/metadata/entities/" + entityId + "/fields").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("sku"));
    }
}
