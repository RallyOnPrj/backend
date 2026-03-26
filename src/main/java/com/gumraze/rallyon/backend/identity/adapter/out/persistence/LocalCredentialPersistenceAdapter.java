package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.LocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.entity.LocalCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocalCredentialPersistenceAdapter implements LoadLocalCredentialPort, SaveLocalCredentialPort {

    private final LocalCredentialRepository repository;

    @Override
    public Optional<LocalCredential> loadByEmailNormalized(String emailNormalized) {
        return repository.findByEmailNormalized(emailNormalized);
    }

    @Override
    public LocalCredential save(LocalCredential credential) {
        return repository.save(credential);
    }
}
