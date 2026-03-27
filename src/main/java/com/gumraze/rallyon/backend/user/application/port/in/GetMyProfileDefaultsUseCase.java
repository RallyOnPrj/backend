package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileDefaultsQuery;
import com.gumraze.rallyon.backend.user.dto.UserProfileDefaultsResponse;

public interface GetMyProfileDefaultsUseCase {

    UserProfileDefaultsResponse get(GetMyProfileDefaultsQuery query);
}
