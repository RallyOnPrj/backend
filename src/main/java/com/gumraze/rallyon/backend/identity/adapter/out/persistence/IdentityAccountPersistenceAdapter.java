package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityAccountRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityAccountPersistenceAdapter implements LoadIdentityAccountPort, SaveIdentityAccountPort {

    private final IdentityAccountRepository identityAccountRepository;

    @Override
    public Optional<IdentityAccount> loadById(UUID identityAccountId) {
        return identityAccountRepository.findById(identityAccountId);
    }

    @Override
    public IdentityAccount save(IdentityAccount identityAccount) {
        return identityAccountRepository.save(identityAccount);
    }
}
