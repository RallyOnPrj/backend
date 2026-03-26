package com.gumraze.rallyon.backend.courtManager.application.port.out;

import java.util.UUID;

public interface LoadAccountPort {

    boolean existsById(UUID accountId);
}
