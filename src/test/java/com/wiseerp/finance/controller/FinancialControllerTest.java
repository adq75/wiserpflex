package com.wiseerp.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseerp.finance.domain.Account;
import com.wiseerp.finance.domain.AccountType;
import com.wiseerp.finance.service.AccountService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.wiseerp.TestApplication.class)
@AutoConfigureMockMvc
public class FinancialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetadataVerificationService verificationService;

    @MockBean
    private AccountService accountService;

        @MockBean
        private com.wiseerp.finance.service.JournalEntryService journalEntryService;

        @MockBean
        private com.wiseerp.finance.service.FiscalPeriodService fiscalPeriodService;

        @MockBean
        private com.wiseerp.finance.service.FinancialReportService financialReportService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void createAccount_returns200_whenMetadataExists() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Account request = Account.builder()
                .code("1000")
                .name("Cash")
                .accountType(AccountType.ASSET)
                .build();

        Account saved = Account.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .code(request.getCode())
                .name(request.getName())
                .accountType(request.getAccountType())
                .build();

        when(verificationService.verifyEntityForTenant(eq("Account"), any(UUID.class))).thenReturn(true);
        when(accountService.createAccount(any(Account.class), any(UUID.class))).thenReturn(saved);

        mockMvc.perform(post("/api/" + tenantId + "/finance/accounts")
                        .with(jwt())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()));
    }

    @Test
    void createAccount_returns412_whenMetadataMissing() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Account request = Account.builder()
                .code("2000")
                .name("Revenue")
                .accountType(AccountType.REVENUE)
                .build();

        when(verificationService.verifyEntityForTenant(eq("Account"), any(UUID.class))).thenReturn(false);

        mockMvc.perform(post("/api/" + tenantId + "/finance/accounts")
                        .with(jwt())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isPreconditionRequired());
    }
}
