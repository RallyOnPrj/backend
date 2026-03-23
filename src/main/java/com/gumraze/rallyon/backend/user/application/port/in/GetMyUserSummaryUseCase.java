package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyUserSummaryQuery;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;

public interface GetMyUserSummaryUseCase {

    UserMeResponse get(GetMyUserSummaryQuery query);
}
