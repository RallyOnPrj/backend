package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;

import java.util.List;
import java.util.UUID;

public interface LoadFreeGameRoundPort {

    List<FreeGameRound> loadRoundsByGameIdOrderByRoundNumber(UUID gameId);
}
