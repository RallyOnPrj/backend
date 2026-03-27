package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadAccountPersistenceAdapter implements LoadAccountPort {

    private final AccountRepository accountRepository;

    @Override
    public boolean existsById(UUID accountId) {
        return accountRepository.existsById(accountId);
    }
}
