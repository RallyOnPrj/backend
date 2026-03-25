package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoadUserOnboardingStatusService implements LoadUserOnboardingStatusUseCase {

    private final LoadUserProfilePort loadUserProfilePort;

    @Override
    public UserStatus load(UUID identityAccountId) {
        return loadUserProfilePort.existsByIdentityAccountId(identityAccountId)
                ? UserStatus.ACTIVE
                : UserStatus.PENDING;
    }
}
