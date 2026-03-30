package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;

public interface SaveFreeGamePort {
    FreeGame save(FreeGame freeGame);
}
