package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.LocalCredential;

import java.util.Optional;

public interface LoadLocalCredentialPort {

    Optional<LocalCredential> loadByEmailNormalized(String emailNormalized);
}
