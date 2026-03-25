package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyProfileServiceTest {

    @Mock
    private LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Mock
    private LoadRegionPort loadRegionPort;

    @Test
    @DisplayName("내 프로필 조회 시 region snapshot으로 district/province 이름을 채운다")
    void get_profile_resolves_region_snapshot() {
        var identityAccountId = uuid(1);
        var districtId = uuid(2);
        UserProfile profile = UserProfile.create(
                identityAccountId,
                "테스트 닉네임",
                districtId,
                Grade.D,
                Grade.C,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                Gender.MALE,
                "AB12",
                LocalDateTime.of(2022, 1, 1, 0, 0)
        );

        GetMyProfileService service = new GetMyProfileService(
                loadUserOnboardingStatusUseCase,
                loadUserProfilePort,
                loadRegionPort
        );

        when(loadUserOnboardingStatusUseCase.load(identityAccountId)).thenReturn(UserStatus.ACTIVE);
        when(loadUserProfilePort.loadByIdentityAccountId(identityAccountId)).thenReturn(Optional.of(profile));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.of(
                new RegionDistrictReference(districtId, "권선구", "41113", uuid(3), "경기도", "41")
        ));

        UserProfileResponseDto result = service.get(new GetMyProfileQuery(identityAccountId));

        assertThat(result.nickname()).isEqualTo("테스트 닉네임");
        assertThat(result.districtName()).isEqualTo("권선구");
        assertThat(result.provinceName()).isEqualTo("경기도");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("프로필의 districtId가 유효하지 않으면 예외를 던진다")
    void get_profile_throws_when_district_reference_missing() {
        var identityAccountId = uuid(1);
        var districtId = uuid(2);
        UserProfile profile = UserProfile.create(
                identityAccountId,
                "테스트 닉네임",
                districtId,
                null,
                null,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                Gender.MALE,
                "AB12",
                LocalDateTime.now()
        );

        GetMyProfileService service = new GetMyProfileService(
                loadUserOnboardingStatusUseCase,
                loadUserProfilePort,
                loadRegionPort
        );

        when(loadUserOnboardingStatusUseCase.load(identityAccountId)).thenReturn(UserStatus.ACTIVE);
        when(loadUserProfilePort.loadByIdentityAccountId(identityAccountId)).thenReturn(Optional.of(profile));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(new GetMyProfileQuery(identityAccountId)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("지역 정보를 찾을 수 없습니다.");
    }
}
