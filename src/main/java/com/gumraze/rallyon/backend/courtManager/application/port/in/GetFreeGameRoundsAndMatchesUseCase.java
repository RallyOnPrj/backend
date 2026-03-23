package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameRoundsAndMatchesQuery;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;

public interface GetFreeGameRoundsAndMatchesUseCase {

    FreeGameRoundMatchResponse get(GetFreeGameRoundsAndMatchesQuery query);
}
