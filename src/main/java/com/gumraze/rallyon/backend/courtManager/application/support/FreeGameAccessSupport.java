package com.gumraze.rallyon.backend.courtManager.application.support;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;

import java.util.UUID;

public final class FreeGameAccessSupport {

    private FreeGameAccessSupport() {
    }

    public static FreeGame loadOrganizerGame(
            LoadFreeGamePort loadFreeGamePort,
            UUID organizerId,
            UUID gameId
    ) {
        FreeGame freeGame = loadFreeGamePort.loadGameById(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임입니다. gameId: " + gameId));

        if (!freeGame.getOrganizerIdentityAccountId().equals(organizerId)) {
            throw new ForbiddenException("게임의 organizer가 아닙니다. gameId: " + gameId);
        }

        return freeGame;
    }
}
