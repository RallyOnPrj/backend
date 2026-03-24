package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityLocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdentityLocalCredentialPersistenceAdapter implements LoadLocalCredentialPort, SaveLocalCredentialPort {

    private final IdentityLocalCredentialRepository repository;

    @Override
    public Optional<IdentityLocalCredential> loadByEmailNormalized(String emailNormalized) {
        return repository.findByEmailNormalized(emailNormalized);
    }

    @Override
    public IdentityLocalCredential save(IdentityLocalCredential credential) {
        return repository.save(credential);
    }
}
