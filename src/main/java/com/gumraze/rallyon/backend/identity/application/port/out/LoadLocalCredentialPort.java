package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;

import java.util.Optional;

public interface LoadLocalCredentialPort {

    Optional<IdentityLocalCredential> loadByEmailNormalized(String emailNormalized);
}
