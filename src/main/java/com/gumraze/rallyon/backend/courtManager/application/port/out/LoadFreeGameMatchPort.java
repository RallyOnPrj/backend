package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;

import java.util.List;
import java.util.UUID;

public interface LoadFreeGameMatchPort {

    List<FreeGameMatch> loadMatchesByRoundIdsOrderByCourtNumber(List<UUID> roundIds);
}
