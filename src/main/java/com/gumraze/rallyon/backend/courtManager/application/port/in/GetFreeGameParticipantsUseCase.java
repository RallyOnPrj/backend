package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantsQuery;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;

public interface GetFreeGameParticipantsUseCase {

    FreeGameParticipantsResponse get(GetFreeGameParticipantsQuery query);
}
