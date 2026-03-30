package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantDetailQuery;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;

public interface GetFreeGameParticipantDetailUseCase {

    FreeGameParticipantDetailResponse get(GetFreeGameParticipantDetailQuery query);
}
