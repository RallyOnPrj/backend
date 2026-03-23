package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.GetMyUserSummaryUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyUserSummaryQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityUserPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyUserSummaryService implements GetMyUserSummaryUseCase {

    private final LoadIdentityUserPort loadIdentityUserPort;
    private final LoadUserProfilePort loadUserProfilePort;

    @Override
    public UserMeResponse get(GetMyUserSummaryQuery query) {
        User user = loadIdentityUserPort.loadById(query.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserProfile profile = null;
        if (user.getStatus() == UserStatus.ACTIVE) {
            profile = loadUserProfilePort.loadByUserId(query.userId()).orElse(null);
        }

        return UserMeResponse.from(user, profile);
    }
}
