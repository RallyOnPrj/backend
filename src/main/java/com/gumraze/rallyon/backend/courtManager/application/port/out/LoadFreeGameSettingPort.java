package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;

import java.util.Optional;
import java.util.UUID;

public interface LoadFreeGameSettingPort {

    Optional<FreeGameSetting> loadSettingByGameId(UUID gameId);
}
