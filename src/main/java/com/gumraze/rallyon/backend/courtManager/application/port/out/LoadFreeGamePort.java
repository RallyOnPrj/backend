package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import java.util.Optional;
import java.util.UUID;

public interface LoadFreeGamePort {
    Optional<FreeGame> loadGameById(UUID gameId);

    Optional<FreeGame> loadGameByShareCode(String shareCode);
}
