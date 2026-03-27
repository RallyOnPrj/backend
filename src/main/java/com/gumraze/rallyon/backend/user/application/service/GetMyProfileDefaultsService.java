package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfileDefaultsUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileDefaultsQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadAccountDisplayNamePort;
import com.gumraze.rallyon.backend.user.dto.UserProfileDefaultsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetMyProfileDefaultsService implements GetMyProfileDefaultsUseCase {

    private final LoadAccountDisplayNamePort loadAccountDisplayNamePort;

    @Override
    public UserProfileDefaultsResponse get(GetMyProfileDefaultsQuery query) {
        Optional<String> nickname = loadAccountDisplayNamePort.loadLatestDisplayName(query.accountId());
        return new UserProfileDefaultsResponse(nickname.orElse(null), nickname.isPresent());
    }
}
