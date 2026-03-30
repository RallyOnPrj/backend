package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.Account;

import java.util.Optional;
import java.util.UUID;

public interface LoadAccountPort {

    Optional<Account> loadById(UUID accountId);
}
