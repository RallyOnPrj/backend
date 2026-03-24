package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityUserPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyProfileServiceTest {

    @Mock
    private LoadIdentityUserPort loadIdentityUserPort;

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Mock
    private LoadRegionPort loadRegionPort;

    @Test
    @DisplayName("내 프로필 조회 시 region snapshot으로 district/province 이름을 채운다")
    void get_profile_resolves_region_snapshot() {
        var userId = uuid(1);
        var districtId = uuid(2);

        GetMyProfileService service = new GetMyProfileService(
                loadIdentityUserPort,
                loadUserProfilePort,
                loadRegionPort
        );

        when(loadIdentityUserPort.loadById(userId)).thenReturn(Optional.of(
                User.builder().id(userId).status(UserStatus.ACTIVE).build()
        ));
        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(
                UserProfile.builder()
                        .user(User.builder().id(userId).build())
                        .nickname("테스트 닉네임")
                        .tag("AB12")
                        .districtId(districtId)
                        .regionalGrade(Grade.D)
                        .nationalGrade(Grade.C)
                        .birth(LocalDateTime.of(2000, 1, 1, 0, 0))
                        .birthVisible(true)
                        .gender(Gender.MALE)
                        .tagChangedAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                        .createdAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                        .updatedAt(LocalDateTime.of(2022, 1, 2, 0, 0))
                        .build()
        ));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.of(
                new RegionDistrictReference(districtId, "권선구", "41113", uuid(3), "경기도", "41")
        ));

        UserProfileResponseDto result = service.get(new GetMyProfileQuery(userId));

        assertThat(result.getNickname()).isEqualTo("테스트 닉네임");
        assertThat(result.getDistrictName()).isEqualTo("권선구");
        assertThat(result.getProvinceName()).isEqualTo("경기도");
    }

    @Test
    @DisplayName("프로필의 districtId가 유효하지 않으면 예외를 던진다")
    void get_profile_throws_when_district_reference_missing() {
        var userId = uuid(1);
        var districtId = uuid(2);

        GetMyProfileService service = new GetMyProfileService(
                loadIdentityUserPort,
                loadUserProfilePort,
                loadRegionPort
        );

        when(loadIdentityUserPort.loadById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(loadUserProfilePort.loadByUserId(userId)).thenReturn(Optional.of(
                UserProfile.builder()
                        .user(User.builder().id(userId).build())
                        .districtId(districtId)
                        .tag("AB12")
                        .tagChangedAt(LocalDateTime.now())
                        .build()
        ));
        when(loadRegionPort.loadDistrictReference(districtId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(new GetMyProfileQuery(userId)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("지역 정보를 찾을 수 없습니다.");
    }
}
