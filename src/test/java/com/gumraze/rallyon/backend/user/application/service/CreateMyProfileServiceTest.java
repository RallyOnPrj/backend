package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserGradeHistoryPort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.domain.UserProfileTagGenerator;
import com.gumraze.rallyon.backend.user.domain.UserProfileValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMyProfileServiceTest {

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
    @DisplayName("프로필 생성 시 districtId와 identityAccountId를 저장한다")
    void create_profile_saves_district_id_and_identity_account_id() {
        UUID identityAccountId = uuid(1);
        UUID districtId = uuid(2);

        CreateMyProfileService service = new CreateMyProfileService(
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                saveUserGradeHistoryPort,
                userProfileValidator,
                userProfileTagGenerator
        );

        when(loadUserProfilePort.existsByIdentityAccountId(identityAccountId)).thenReturn(false);
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.of(
                new RegionDistrictReference(districtId, "권선구", "41113", uuid(3), "경기도", "41")
        ));
        when(userProfileTagGenerator.generate()).thenReturn("AB12");

        service.create(new CreateMyProfileCommand(
                identityAccountId,
                "kim",
                districtId,
                Grade.B,
                Grade.C,
                "19980925",
                Gender.MALE
        ));

        verify(saveUserProfilePort).save(argThat(profile ->
                profile.getIdentityAccountId().equals(identityAccountId)
                        && profile.getDistrictId().equals(districtId)
                        && profile.getTag().equals("AB12")
        ));
        verify(saveUserGradeHistoryPort, times(2)).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 districtId면 프로필 생성이 실패한다")
    void create_profile_throws_when_district_missing() {
        UUID identityAccountId = uuid(1);
        UUID districtId = uuid(2);

        CreateMyProfileService service = new CreateMyProfileService(
                loadUserProfilePort,
                saveUserProfilePort,
                loadRegionPort,
                saveUserGradeHistoryPort,
                userProfileValidator,
                userProfileTagGenerator
        );

        when(loadUserProfilePort.existsByIdentityAccountId(identityAccountId)).thenReturn(false);
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateMyProfileCommand(
                identityAccountId,
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
