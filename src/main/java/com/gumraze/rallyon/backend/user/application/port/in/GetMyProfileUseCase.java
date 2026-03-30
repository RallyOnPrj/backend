package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileQuery;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;

public interface GetMyProfileUseCase {

    UserProfileResponseDto get(GetMyProfileQuery query);
}
