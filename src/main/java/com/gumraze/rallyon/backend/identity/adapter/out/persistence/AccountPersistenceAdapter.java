package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveAccountPort;
import com.gumraze.rallyon.backend.identity.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final AccountRepository accountRepository;

    @Override
    public Optional<Account> loadById(UUID accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }
}
