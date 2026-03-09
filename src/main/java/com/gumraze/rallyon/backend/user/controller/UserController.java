package com.gumraze.rallyon.backend.user.controller;

import com.gumraze.rallyon.backend.api.user.UserApi;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.user.dto.*;
import com.gumraze.rallyon.backend.user.entity.UserProfileUpdateRequest;
import com.gumraze.rallyon.backend.user.service.UserProfileService;
import com.gumraze.rallyon.backend.user.service.UserSearchService;
import com.gumraze.rallyon.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController implements UserApi {

    private final UserProfileService userProfileService;
    private final UserSearchService userSearchService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserSearchResponse>> searchUsers(
            @RequestParam String nickname,
            @RequestParam(required = false) String tag,
            Pageable pageable
    ) {
        Page<UserSearchResponse> body;

        if (tag == null || tag.isBlank()) {
            body = userSearchService.searchByNickname(nickname, pageable);
        } else {
            UserSearchResponse found = userSearchService.searchByNicknameAndTag(nickname, tag)
                    .orElseThrow(() -> new NotFoundException("유저가 없습니다."));
            body = new PageImpl<>(List.of(found), pageable, 1);
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(
            @AuthenticationPrincipal Long userId
    ) {
        UserMeResponse body = userService.getUserMe(userId);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/me/profile")
    public ResponseEntity<UserProfileCreateResponseDto> createProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileCreateRequest request
    ) {
        userProfileService.createProfile(userId, request);
        UserProfileCreateResponseDto body = UserProfileCreateResponseDto.builder().userId(userId).build();
        URI location = URI.create("/users/me/profile");
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/me/profile/prefill")
    public ResponseEntity<UserProfilePrefillResponseDto> prefillProfile(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfilePrefillResponseDto body = userProfileService.getProfilePrefill(userId);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfileResponseDto body = userProfileService.getMyProfile(userId);

        return ResponseEntity.ok(body);
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        userProfileService.updateMyProfile(userId, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/profile/identity")
    public ResponseEntity<Void> updateIdentity(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileIdentityUpdateRequest request
    ) {
        userProfileService.updateNicknameAndTags(userId, request);

        return ResponseEntity.noContent().build();
    }
}