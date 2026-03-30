package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

    private final LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;
    private final LoadUserProfilePort loadUserProfilePort;
    private final LoadRegionPort loadRegionPort;

    @Override
    public UserProfileResponseDto get(GetMyProfileQuery query) {
        var status = loadUserOnboardingStatusUseCase.load(query.accountId());
        UserProfile profile = loadUserProfilePort.loadByAccountId(query.accountId())
                .orElseThrow(() -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다."));
        var districtReference = profile.getDistrictId() == null
                ? null
                : loadRegionPort.loadDistrictReference(profile.getDistrictId())
                .orElseThrow(() -> new NotFoundException("지역 정보를 찾을 수 없습니다."));

        return new UserProfileResponseDto(
                status,
                profile.getNickname(),
                profile.getTag(),
                profile.getProfileImageUrl(),
                profile.getGender(),
                profile.getBirth(),
                profile.isBirthVisible(),
                profile.getRegionalGrade(),
                profile.getNationalGrade(),
                districtReference == null ? null : districtReference.districtName(),
                districtReference == null ? null : districtReference.provinceName(),
                profile.getTagChangedAt(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
