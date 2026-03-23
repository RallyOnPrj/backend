package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfilePrefillUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfilePrefillQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityDisplayNamePort;
import com.gumraze.rallyon.backend.user.dto.UserProfilePrefillResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetMyProfilePrefillService implements GetMyProfilePrefillUseCase {

    private final LoadIdentityDisplayNamePort loadIdentityDisplayNamePort;

    @Override
    public UserProfilePrefillResponseDto get(GetMyProfilePrefillQuery query) {
        Optional<String> nickname = loadIdentityDisplayNamePort.loadLatestDisplayName(query.userId());
        return new UserProfilePrefillResponseDto(nickname.orElse(null), nickname.isPresent());
    }
}
