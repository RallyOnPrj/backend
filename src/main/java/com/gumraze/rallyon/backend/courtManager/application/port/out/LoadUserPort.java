package com.gumraze.rallyon.backend.courtManager.application.port.out;

import java.util.UUID;

public interface LoadUserPort {

    boolean existsById(UUID identityAccountId);
}
