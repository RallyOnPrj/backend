package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.constants.UserStatus;

import java.util.UUID;

public interface LoadUserOnboardingStatusUseCase {

    UserStatus load(UUID identityAccountId);
}
