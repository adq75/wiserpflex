package com.wiseerp.finance.service;

import com.wiseerp.finance.domain.Account;
import com.wiseerp.finance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public Account createAccount(Account account, UUID tenantId) {
        if (account.getId() == null) {
            account.setId(UUID.randomUUID());
        }
        account.setTenantId(tenantId);
        return accountRepository.save(account);
    }
}
