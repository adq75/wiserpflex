package com.wiseerp.metadata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.metadata.dto.EntityDefinitionDto;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.service.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
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
public class MetadataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetadataService metadataService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
    }

    @Test
    void createAndListEntity() throws Exception {
        UUID id = UUID.randomUUID();
        EntityDefinition saved = EntityDefinition.builder()
                .id(id)
                .tenantId(tenantId)
                .name("Product")
                .displayName("Product")
                .isActive(Boolean.TRUE)
                .createdAt(Instant.now())
                .build();

        when(metadataService.createEntityDefinition(any(EntityDefinition.class))).thenReturn(saved);
        when(metadataService.listByTenant(tenantId)).thenReturn(List.of(saved));

        EntityDefinitionDto req = EntityDefinitionDto.builder().name("Product").build();

        mockMvc.perform(post("/api/" + tenantId + "/metadata/entities")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Product"));

        mockMvc.perform(get("/api/" + tenantId + "/metadata/entities").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product"));
    }
}
