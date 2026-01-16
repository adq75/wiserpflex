package com.wiseerp.metadata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.metadata.service.MetadataVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.wiseerp.TestApplication.class)
@AutoConfigureMockMvc
public class MetadataVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetadataVerificationService verificationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private com.wiseerp.finance.repository.AccountRepository accountRepository;

    @MockBean
    private com.wiseerp.finance.service.JournalEntryService journalEntryService;

    @MockBean
    private com.wiseerp.finance.service.FiscalPeriodService fiscalPeriodService;

    @MockBean
    private com.wiseerp.finance.service.FinancialReportService financialReportService;

    @Test
    void verifyEntity_returnsExistsTrue_whenServiceReportsTrue() throws Exception {
        UUID tenantId = UUID.randomUUID();
        String entityName = "Account";

        when(verificationService.verifyEntityForTenant(eq(entityName), any(UUID.class))).thenReturn(true);

        mockMvc.perform(get("/api/" + tenantId + "/metadata/verify")
                        .with(jwt())
                        .param("entityName", entityName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityName").value(entityName))
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void verifyEntity_returnsExistsFalse_whenServiceReportsFalse() throws Exception {
        UUID tenantId = UUID.randomUUID();
        String entityName = "NonExistent";

        when(verificationService.verifyEntityForTenant(eq(entityName), any(UUID.class))).thenReturn(false);

        mockMvc.perform(get("/api/" + tenantId + "/metadata/verify")
                        .with(jwt())
                        .param("entityName", entityName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityName").value(entityName))
                .andExpect(jsonPath("$.exists").value(false));
    }
}
