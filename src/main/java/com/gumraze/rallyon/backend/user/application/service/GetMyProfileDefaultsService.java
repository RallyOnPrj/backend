package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfileDefaultsUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileDefaultsQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityDisplayNamePort;
import com.gumraze.rallyon.backend.user.dto.UserProfileDefaultsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetMyProfileDefaultsService implements GetMyProfileDefaultsUseCase {

    private final LoadIdentityDisplayNamePort loadIdentityDisplayNamePort;

    @Override
    public UserProfileDefaultsResponse get(GetMyProfileDefaultsQuery query) {
        Optional<String> nickname = loadIdentityDisplayNamePort.loadLatestDisplayName(query.identityAccountId());
        return new UserProfileDefaultsResponse(nickname.orElse(null), nickname.isPresent());
    }
}
