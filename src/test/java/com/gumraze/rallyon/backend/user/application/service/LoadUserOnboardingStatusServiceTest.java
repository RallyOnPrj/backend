package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoadUserOnboardingStatusServiceTest {

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @InjectMocks
    private LoadUserOnboardingStatusService service;

    @Test
    @DisplayName("프로필이 존재하면 ACTIVE를 반환한다")
    void load_returns_active_when_profile_exists() {
        UUID identityAccountId = UUID.randomUUID();
        given(loadUserProfilePort.existsByIdentityAccountId(identityAccountId)).willReturn(true);

        assertThat(service.load(identityAccountId)).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("프로필이 없으면 PENDING을 반환한다")
    void load_returns_pending_when_profile_does_not_exist() {
        UUID identityAccountId = UUID.randomUUID();
        given(loadUserProfilePort.existsByIdentityAccountId(identityAccountId)).willReturn(false);

        assertThat(service.load(identityAccountId)).isEqualTo(UserStatus.PENDING);
    }
}
