package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.UnprocessableEntityException;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyPublicIdentityCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMyPublicIdentityServiceTest {

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Mock
    private SaveUserProfilePort saveUserProfilePort;

    @Test
    @DisplayName("닉네임과 태그 수정 시 태그를 정규화하고 tagChangedAt을 갱신한다")
    void update_public_identity_normalizes_tag() {
        var userId = uuid(1);
        var profile = UserProfile.builder()
                .user(User.builder().id(userId).build())
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now().minusDays(91))
                .build();

        UpdateMyPublicIdentityService service = new UpdateMyPublicIdentityService(
                loadUserProfilePort,
                saveUserProfilePort
        );

        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(profile));
        when(loadUserProfilePort.loadByNicknameAndTag("newNickname", "SON7")).thenReturn(Optional.empty());

        service.update(new UpdateMyPublicIdentityCommand(userId, "newNickname", "son7"));

        assertThat(profile.getNickname()).isEqualTo("newNickname");
        assertThat(profile.getTag()).isEqualTo("SON7");
        assertThat(profile.getTagChangedAt()).isAfter(LocalDateTime.now().minusDays(2));
        verify(saveUserProfilePort).save(profile);
    }

    @Test
    @DisplayName("90일 이내 태그 재변경은 실패한다")
    void update_public_identity_throws_when_tag_changed_too_soon() {
        var userId = uuid(1);
        var profile = UserProfile.builder()
                .user(User.builder().id(userId).build())
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now().minusDays(30))
                .build();

        UpdateMyPublicIdentityService service = new UpdateMyPublicIdentityService(
                loadUserProfilePort,
                saveUserProfilePort
        );

        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.update(new UpdateMyPublicIdentityCommand(userId, null, "new1")))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("태그 변경은 90일 이내에 한 번만 가능합니다.");
    }
}
