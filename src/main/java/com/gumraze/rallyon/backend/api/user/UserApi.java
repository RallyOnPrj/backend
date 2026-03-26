package com.gumraze.rallyon.backend.api.user;

import com.gumraze.rallyon.backend.api.common.ApiAuthValidationResponses;
import com.gumraze.rallyon.backend.api.common.ApiBearerAuth;
import com.gumraze.rallyon.backend.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Users", description = "사용자 API")
@ApiBearerAuth
public interface UserApi {

    @Operation(
            summary = "사용자 검색",
            description = "닉네임으로 사용자를 검색하고, tag가 있으면 태그 조건을 함께 적용합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageImpl.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저가 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<Page<UserSearchResponse>> searchUsers(
            @Parameter(description = "검색할 닉네임", required = true)
            String nickname,
            @Parameter(description = "닉네임과 함께 사용할 태그(선택)")
            String tag,
            Pageable pageable
    );

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필을 조회합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserMeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<UserMeResponse> me (
            UUID accountId
    );

    @Operation(
            summary = "프로필 생성",
            description = "닉네임/지역/등급을 입력해 프로필을 생성하고 온보딩 상태를 ACTIVE로 전환합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "프로필 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileCreateResponseDto.class)
                    )
            )
    })
    ResponseEntity<UserProfileCreateResponseDto>
    createProfile(
            UUID accountId,
            UserProfileCreateRequest request
    );

    @Operation(
            summary = "프로필 초기값 조회",
            description = "제3자 로그인 닉네임이 있으면 suggestedNickname으로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDefaultsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<UserProfileDefaultsResponse> profileDefaults(
            UUID accountId
    );

    @Operation(
            summary = "내 프로필 상세 조회",
            description = "현재 로그인한 사용자의 프로필 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자의 프로필을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<UserProfileResponseDto> getMyProfile(
            UUID accountId
    );

    @Operation(
            summary = "내 프로필 수정",
            description = "내 프로필 정보를 수정합니다. 닉네임과 태그도 같은 aggregate에서 함께 수정합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "내 프로필 수정 성공 (No Content)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자의 프로필을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<Void> updateMyProfile(
            UUID accountId,
            UserProfileUpdateRequest request
    );
}
