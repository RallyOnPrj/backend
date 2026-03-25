package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.common.exception.UnprocessableEntityException;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.service.UserProfileValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMyProfileServiceTest {

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Mock
    private SaveUserProfilePort saveUserProfilePort;

    @Mock
    private LoadRegionPort loadRegionPort;

    @Test
    @DisplayName("프로필 수정 시 districtId를 새 snapshot 기준으로 갱신한다")
    void update_profile_updates_requested_fields() {
        var userId = uuid(1);
        var newDistrictId = uuid(2);
        var profile = UserProfile.builder()
                .user(User.builder().id(userId).build())
                .nickname("oldNickname")
                .tag("AB12")
                .districtId(uuid(99))
                .birth(LocalDate.of(1998, 9, 25).atStartOfDay())
                .birthVisible(true)
                .gender(Gender.MALE)
                .regionalGrade(Grade.D)
                .nationalGrade(Grade.D)
                .build();

        UpdateMyProfileService service = new UpdateMyProfileService(
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                new UserProfileValidator()
        );

        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(profile));
        when(loadRegionPort.loadDistrictReference(newDistrictId)).thenReturn(Optional.of(
                new RegionDistrictReference(newDistrictId, "권선구", "41113", uuid(3), "경기도", "41")
        ));
        when(loadUserProfilePort.loadByNicknameAndTag("newNickname", "SON7")).thenReturn(Optional.empty());

        service.update(new UpdateMyProfileCommand(
                userId,
                "newNickname",
                "son7",
                Grade.B,
                Grade.C,
                "19980925",
                false,
                newDistrictId,
                "https://example.com/profile.jpg",
                Gender.FEMALE
        ));

        assertThat(profile.getRegionalGrade()).isEqualTo(Grade.B);
        assertThat(profile.getNationalGrade()).isEqualTo(Grade.C);
        assertThat(profile.getNickname()).isEqualTo("newNickname");
        assertThat(profile.getTag()).isEqualTo("SON7");
        assertThat(profile.getDistrictId()).isEqualTo(newDistrictId);
        assertThat(profile.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
        verify(saveUserProfilePort).save(profile);
    }

    @Test
    @DisplayName("존재하지 않는 districtId면 프로필 수정이 실패한다")
    void update_profile_throws_when_district_missing() {
        var userId = uuid(1);
        var districtId = uuid(2);
        var profile = UserProfile.builder()
                .user(User.builder().id(userId).build())
                .tag("AB12")
                .tagChangedAt(java.time.LocalDateTime.now())
                .build();

        UpdateMyProfileService service = new UpdateMyProfileService(
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                new UserProfileValidator()
        );

        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(profile));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateMyProfileCommand(
                userId,
                null,
                null,
                null,
                null,
                null,
                null,
                districtId,
                null,
                null
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지역이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("태그 변경은 90일 이내에 한 번만 가능하다")
    void update_profile_throws_when_tag_changed_too_soon() {
        var userId = uuid(1);
        var profile = UserProfile.builder()
                .user(User.builder().id(userId).build())
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(java.time.LocalDateTime.now().minusDays(30))
                .build();

        UpdateMyProfileService service = new UpdateMyProfileService(
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                new UserProfileValidator()
        );

        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.update(new UpdateMyProfileCommand(
                userId,
                null,
                "new1",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ))).isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("태그 변경은 90일 이내에 한 번만 가능합니다.");
    }
}
