package com.gumraze.rallyon.backend.user.adapter.in.web;

import com.gumraze.rallyon.backend.api.user.UserApi;
import com.gumraze.rallyon.backend.user.application.port.in.*;
import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyPublicIdentityCommand;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfilePrefillQuery;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileQuery;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyUserSummaryQuery;
import com.gumraze.rallyon.backend.user.application.port.in.query.SearchUsersQuery;
import com.gumraze.rallyon.backend.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final SearchUsersUseCase searchUsersUseCase;
    private final GetMyUserSummaryUseCase getMyUserSummaryUseCase;
    private final CreateMyProfileUseCase createMyProfileUseCase;
    private final GetMyProfilePrefillUseCase getMyProfilePrefillUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final UpdateMyPublicIdentityUseCase updateMyPublicIdentityUseCase;

    @Override
    @GetMapping
    public ResponseEntity<Page<UserSearchResponse>> searchUsers(
            @RequestParam String nickname,
            @RequestParam(required = false) String tag,
            Pageable pageable
    ) {
        return ResponseEntity.ok(searchUsersUseCase.search(new SearchUsersQuery(nickname, tag, pageable)));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(getMyUserSummaryUseCase.get(new GetMyUserSummaryQuery(userId)));
    }

    @Override
    @PostMapping("/me/profile")
    public ResponseEntity<UserProfileCreateResponseDto> createProfile(
            @AuthenticationPrincipal UUID userId,
            @RequestBody UserProfileCreateRequest request
    ) {
        createMyProfileUseCase.create(new CreateMyProfileCommand(
                userId,
                request.getNickname(),
                request.getDistrictId(),
                request.getRegionalGrade(),
                request.getNationalGrade(),
                request.getBirth(),
                request.getGender()
        ));
        return ResponseEntity.created(URI.create("/users/me/profile"))
                .body(UserProfileCreateResponseDto.builder().userId(userId).build());
    }

    @Override
    @GetMapping("/me/profile/prefill")
    public ResponseEntity<UserProfilePrefillResponseDto> prefillProfile(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(getMyProfilePrefillUseCase.get(new GetMyProfilePrefillQuery(userId)));
    }

    @Override
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(getMyProfileUseCase.get(new GetMyProfileQuery(userId)));
    }

    @Override
    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal UUID userId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        updateMyProfileUseCase.update(new UpdateMyProfileCommand(
                userId,
                request.getRegionalGrade(),
                request.getNationalGrade(),
                request.getBirth(),
                request.getBirthVisible(),
                request.getDistrictId(),
                request.getProfileImageUrl(),
                request.getGender()
        ));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/me/profile/identity")
    public ResponseEntity<Void> updateIdentity(
            @AuthenticationPrincipal UUID userId,
            @RequestBody UserProfileIdentityUpdateRequest request
    ) {
        updateMyPublicIdentityUseCase.update(new UpdateMyPublicIdentityCommand(
                userId,
                request.getNickname(),
                request.getTag()
        ));
        return ResponseEntity.noContent().build();
    }
}
