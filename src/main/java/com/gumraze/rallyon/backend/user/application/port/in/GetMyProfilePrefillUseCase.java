package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfilePrefillQuery;
import com.gumraze.rallyon.backend.user.dto.UserProfilePrefillResponseDto;

public interface GetMyProfilePrefillUseCase {

    UserProfilePrefillResponseDto get(GetMyProfilePrefillQuery query);
}
