package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;

public interface SaveFreeGameSettingPort {

    void save(FreeGame freeGame, Integer courtCount, Integer roundCount);

}
