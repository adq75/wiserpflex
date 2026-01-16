package com.wiseerp.finance;

import com.wiseerp.finance.domain.Account;
import com.wiseerp.finance.domain.AccountType;
import com.wiseerp.finance.service.AccountService;
import com.wiseerp.finance.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class AccountServiceTest {

    @Test
    void createAccount_setsTenantAndSaves() {
        AccountRepository repo = Mockito.mock(AccountRepository.class);
        AccountService svc = new AccountService(repo);

        Account a = Account.builder()
                .code("1000")
                .name("Cash")
                .accountType(AccountType.ASSET)
                .build();

        UUID tenantId = UUID.randomUUID();
        svc.createAccount(a, tenantId);

        verify(repo).save(any(Account.class));
    }
}
