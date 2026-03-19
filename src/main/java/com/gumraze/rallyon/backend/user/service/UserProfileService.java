package com.gumraze.rallyon.backend.user.service;

import com.gumraze.rallyon.backend.user.dto.UserProfileCreateRequest;
import com.gumraze.rallyon.backend.user.dto.UserProfileIdentityUpdateRequest;
import com.gumraze.rallyon.backend.user.dto.UserProfilePrefillResponseDto;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
import com.gumraze.rallyon.backend.user.entity.UserProfileUpdateRequest;

import java.util.UUID;

public interface UserProfileService {

    // 프로필 신규 생성
    void createProfile(UUID userId, UserProfileCreateRequest request);

    // 프로필 업데이트
    void updateProfile(UUID userId, UserProfileCreateRequest request);

    // 제3자 로그인 계정의 닉네임 요청
    UserProfilePrefillResponseDto getProfilePrefill(UUID userId);

    // 프로필 조회
    UserProfileResponseDto getMyProfile(UUID userId);

    // 프로필 수정
    void updateMyProfile(UUID userId, UserProfileUpdateRequest request);

    // 닉네임 + 태그 수정
    void updateNicknameAndTags(UUID userId, UserProfileIdentityUpdateRequest request);
}
