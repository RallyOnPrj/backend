package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;

import java.util.Optional;
import java.util.UUID;

public interface LoadIdentityAccountPort {

    Optional<IdentityAccount> loadById(UUID identityAccountId);
}
