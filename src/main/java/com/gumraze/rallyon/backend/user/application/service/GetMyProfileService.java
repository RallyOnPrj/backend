package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityUserPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

    private final LoadIdentityUserPort loadIdentityUserPort;
    private final LoadUserProfilePort loadUserProfilePort;
    private final LoadRegionPort loadRegionPort;

    @Override
    public UserProfileResponseDto get(GetMyProfileQuery query) {
        User user = loadIdentityUserPort.loadById(query.userId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        UserProfile profile = loadUserProfilePort.loadByUserId(query.userId())
                .orElseThrow(() -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다."));
        var districtReference = profile.getDistrictId() == null
                ? null
                : loadRegionPort.loadDistrictReference(profile.getDistrictId())
                .orElseThrow(() -> new NotFoundException("지역 정보를 찾을 수 없습니다."));

        return UserProfileResponseDto.builder()
                .status(user.getStatus())
                .nickname(profile.getNickname())
                .tag(profile.getTag())
                .profileImageUrl(profile.getProfileImageUrl())
                .birth(profile.getBirth())
                .birthVisible(profile.isBirthVisible())
                .gender(profile.getGender())
                .regionalGrade(profile.getRegionalGrade())
                .nationalGrade(profile.getNationalGrade())
                .districtName(districtReference == null ? null : districtReference.districtName())
                .provinceName(districtReference == null ? null : districtReference.provinceName())
                .tagChangedAt(profile.getTagChangedAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
