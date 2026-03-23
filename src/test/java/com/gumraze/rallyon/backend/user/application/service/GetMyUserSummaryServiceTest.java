package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyUserSummaryQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityUserPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyUserSummaryServiceTest {

    @Mock
    private LoadIdentityUserPort loadIdentityUserPort;

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Test
    @DisplayName("PENDING 사용자는 프로필 조회 없이 status만 반환한다")
    void get_summary_returns_status_only_for_pending_user() {
        var userId = uuid(1);
        GetMyUserSummaryService service = new GetMyUserSummaryService(loadIdentityUserPort, loadUserProfilePort);

        when(loadIdentityUserPort.loadById(userId)).thenReturn(Optional.of(
                User.builder().id(userId).status(UserStatus.PENDING).build()
        ));

        UserMeResponse result = service.get(new GetMyUserSummaryQuery(userId));

        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(result.getNickname()).isNull();
        verify(loadUserProfilePort, never()).loadByUserId(userId);
    }

    @Test
    @DisplayName("ACTIVE 사용자는 프로필 정보를 포함한다")
    void get_summary_returns_profile_for_active_user() {
        var userId = uuid(1);
        GetMyUserSummaryService service = new GetMyUserSummaryService(loadIdentityUserPort, loadUserProfilePort);

        when(loadIdentityUserPort.loadById(userId)).thenReturn(Optional.of(
                User.builder().id(userId).status(UserStatus.ACTIVE).build()
        ));
        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(
                UserProfile.builder()
                        .user(User.builder().id(userId).build())
                        .nickname("테스트 닉네임")
                        .profileImageUrl("https://example.com/profile.png")
                        .build()
        ));

        UserMeResponse result = service.get(new GetMyUserSummaryQuery(userId));

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getNickname()).isEqualTo("테스트 닉네임");
        assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
    }
}
