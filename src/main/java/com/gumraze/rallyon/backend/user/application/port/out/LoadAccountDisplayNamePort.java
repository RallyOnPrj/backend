package com.gumraze.rallyon.backend.user.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface LoadAccountDisplayNamePort {

    Optional<String> loadLatestDisplayName(UUID accountId);
}
