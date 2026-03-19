package com.gumraze.rallyon.backend.user.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.region.entity.RegionDistrict;
import com.gumraze.rallyon.backend.region.entity.RegionProvince;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserRole;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.repository.UserProfileRepository;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetMyProfileDetailUseCaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Test
    @DisplayName("내 프로필 상세 조회 성공 테스트")
    void get_my_profile_detail_success() {
        // given
        UUID userId = uuid(1);
        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        RegionProvince regionProvince = mock(RegionProvince.class);
        RegionDistrict regionDistrict = mock(RegionDistrict.class);



        UserProfile profile = UserProfile.builder()
                .id(uuid(1))
                .user(user)
                .nickname("테스트 닉네임")
                .profileImageUrl("http://profile-image.com")
                .birth(LocalDateTime.now())
                .birthVisible(true)
                .regionDistrict(regionDistrict)
                .regionalGrade(Grade.D)
                .nationalGrade(Grade.D)
                .gender(Gender.MALE)
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(regionProvince.getName()).thenReturn("테스트 시/도");
        when(regionDistrict.getName()).thenReturn("테스트 구");
        when(regionDistrict.getProvince()).thenReturn(regionProvince);

        // when
        UserProfileResponseDto result = userProfileService.getMyProfile(userId);

        // then
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getNickname()).isEqualTo("테스트 닉네임");
        assertThat(result.getTag()).isEqualTo("AB12");
        assertThat(result.getProfileImageUrl()).isEqualTo("http://profile-image.com");
        assertThat(result.getBirth()).isNotNull();
        assertThat(result.isBirthVisible()).isTrue();
        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getRegionalGrade()).isEqualTo(Grade.D);
        assertThat(result.getNationalGrade()).isEqualTo(Grade.D);
        assertThat(result.getDistrictName()).isEqualTo("테스트 구");
        assertThat(result.getProvinceName()).isEqualTo("테스트 시/도");
    }

    @Test
    @DisplayName("프로필이 없으면 NotFoundException 발생")
    void get_my_profile_throws_when_profile_missing() {
        // given
        UUID userId = uuid(1);

        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getMyProfile(userId))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자가 없으면 NotFoundException 발생")
    void get_my_profile_throws_when_user_missing() {
        // given
        UUID userId = uuid(1);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getMyProfile(userId))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(userProfileRepository);
    }
}
