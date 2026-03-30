package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;

public interface GetFreeGameDetailUseCase {

    FreeGameDetailResponse get(GetFreeGameDetailQuery query);
}
