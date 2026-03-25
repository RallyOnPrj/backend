package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityAccountRepository;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadUserPersistenceAdapter implements LoadUserPort {

    private final IdentityAccountRepository identityAccountRepository;

    @Override
    public boolean existsById(UUID identityAccountId) {
        return identityAccountRepository.existsById(identityAccountId);
    }
}
