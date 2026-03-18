package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveFreeGamePersistenceAdapter implements SaveFreeGamePort {

    private final GameRepository gameRepository;

    @Override
    public FreeGame save(FreeGame freeGame) {
        return gameRepository.save(freeGame);
    }
}
