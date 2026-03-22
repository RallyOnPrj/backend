package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public interface LoadFreeGamePort {
    Optional<FreeGame> loadById(UUID gameId);
}
