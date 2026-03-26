package com.gumraze.rallyon.backend.user.adapter.in.web;

import com.gumraze.rallyon.backend.api.user.UserApi;
import com.gumraze.rallyon.backend.user.application.port.in.*;
import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileDefaultsQuery;
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
    private final GetMyProfileDefaultsUseCase getMyProfileDefaultsUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;

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
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(getMyUserSummaryUseCase.get(new GetMyUserSummaryQuery(accountId)));
    }

    @Override
    @PostMapping("/me/profile")
    public ResponseEntity<UserProfileCreateResponseDto> createProfile(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody UserProfileCreateRequest request
    ) {
        createMyProfileUseCase.create(new CreateMyProfileCommand(
                accountId,
                request.nickname(),
                request.districtId(),
                request.regionalGrade(),
                request.nationalGrade(),
                request.birth(),
                request.gender()
        ));
        return ResponseEntity.created(URI.create("/users/me/profile"))
                .body(new UserProfileCreateResponseDto(accountId));
    }

    @Override
    @GetMapping("/me/profile/defaults")
    public ResponseEntity<UserProfileDefaultsResponse> profileDefaults(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(getMyProfileDefaultsUseCase.get(new GetMyProfileDefaultsQuery(accountId)));
    }

    @Override
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(getMyProfileUseCase.get(new GetMyProfileQuery(accountId)));
    }

    @Override
    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        updateMyProfileUseCase.update(new UpdateMyProfileCommand(
                accountId,
                request.nickname(),
                request.tag(),
                request.regionalGrade(),
                request.nationalGrade(),
                request.birth(),
                request.birthVisible(),
                request.districtId(),
                request.profileImageUrl(),
                request.gender()
        ));
        return ResponseEntity.noContent().build();
    }
}
