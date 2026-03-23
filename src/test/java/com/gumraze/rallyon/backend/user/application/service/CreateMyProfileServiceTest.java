package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityUserPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserGradeHistoryPort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.domain.UserProfileTagGenerator;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.service.UserProfileValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMyProfileServiceTest {

    @Mock
    private LoadIdentityUserPort loadIdentityUserPort;

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Mock
    private SaveUserProfilePort saveUserProfilePort;

    @Mock
    private LoadRegionPort loadRegionPort;

    @Mock
    private SaveUserGradeHistoryPort saveUserGradeHistoryPort;

    @Mock
    private UserProfileTagGenerator userProfileTagGenerator;

    private final UserProfileValidator userProfileValidator = new UserProfileValidator();

    @Test
    @DisplayName("프로필 생성 시 districtId를 저장하고 사용자를 ACTIVE로 전환한다")
    void create_profile_saves_district_id_and_activates_user() {
        UUID userId = uuid(1);
        UUID districtId = uuid(2);
        User user = User.builder().id(userId).status(UserStatus.PENDING).build();

        CreateMyProfileService service = new CreateMyProfileService(
                loadIdentityUserPort,
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                saveUserGradeHistoryPort,
                userProfileValidator,
                userProfileTagGenerator
        );

        when(loadUserProfilePort.existsByUserId(userId)).thenReturn(false);
        when(loadIdentityUserPort.loadById(userId)).thenReturn(Optional.of(user));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.of(
                new RegionDistrictReference(districtId, "권선구", "41113", uuid(3), "경기도", "41")
        ));
        when(userProfileTagGenerator.generate()).thenReturn("AB12");
        when(saveUserProfilePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new CreateMyProfileCommand(
                userId,
                "kim",
                districtId,
                Grade.B,
                Grade.C,
                "19980925",
                Gender.MALE
        ));

        verify(saveUserProfilePort).save(argThat(profile ->
                profile.getUser().getId().equals(userId)
                        && profile.getDistrictId().equals(districtId)
                        && profile.getTag().equals("AB12")
        ));
        verify(saveUserGradeHistoryPort, times(2)).save(any());
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 districtId면 프로필 생성이 실패한다")
    void create_profile_throws_when_district_missing() {
        UUID userId = uuid(1);
        UUID districtId = uuid(2);
        User user = User.builder().id(userId).build();

        CreateMyProfileService service = new CreateMyProfileService(
                loadIdentityUserPort,
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                saveUserGradeHistoryPort,
                userProfileValidator,
                userProfileTagGenerator
        );

        when(loadUserProfilePort.existsByUserId(userId)).thenReturn(false);
        when(loadIdentityUserPort.loadById(userId)).thenReturn(Optional.of(user));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateMyProfileCommand(
                userId,
                "kim",
                districtId,
                Grade.B,
                null,
                "19980925",
                Gender.MALE
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지역이 존재하지 않습니다.");

        verify(saveUserProfilePort, never()).save(any());
    }
}
