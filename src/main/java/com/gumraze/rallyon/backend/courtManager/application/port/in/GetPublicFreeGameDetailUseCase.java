package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetPublicFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;

public interface GetPublicFreeGameDetailUseCase {

    FreeGameDetailResponse get(GetPublicFreeGameDetailQuery query);
}
