package com.gumraze.drive.drive_backend.api.courtManager;

import com.gumraze.drive.drive_backend.api.common.ApiAuthValidationResponses;
import com.gumraze.drive.drive_backend.api.common.ApiBearerAuth;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "CourtManager", description = "코트 매니저 API")
@ApiBearerAuth
public interface CourtManagerApi {

    @Operation(
            summary = "자유게임 생성",
            description = "새로운 자유게임을 생성합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "자유게임 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateFreeGameResponse.class)
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
    ResponseEntity<CreateFreeGameResponse> createFreeGame(
            Long userId,
            CreateFreeGameRequest request
    );

    @Operation(
            summary = "자유게임 상세 조회",
            description = "자유게임의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자유게임 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FreeGameDetailResponse.class)
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
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<FreeGameDetailResponse> getFreeGameDetail(
            Long userId,
            Long gameId
    );

    @Operation(
            summary = "자유게임 기본 정보 수정",
            description = "자유게임의 기본 정보를 수정합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자유게임 기본 정보 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateFreeGameResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<UpdateFreeGameResponse> updateFreeGameInfo(
            Long userId,
            Long gameId,
            UpdateFreeGameRequest request
    );

    @Operation(
            summary = "자유게임 라운드 및 매치 조회",
            description = "자유게임 라운드 및 매치 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자유게임 라운드 및 매치 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FreeGameRoundMatchResponse.class)
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
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<FreeGameRoundMatchResponse> getFreeGameRoundMatchResponse(
            Long userId,
            Long gameId
    );

    @Operation(
            summary = "자유게임 라운드 및 매치 수정",
            description = "자유게임 라운드 및 매치 정보를 수정합니다."
    )
    @ApiAuthValidationResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "자유게임 라운드 및 매치 정보 수정 성공 (No Content)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<Void> updateFreeGameRoundMatch(
            Long userId,
            Long gameId,
            UpdateFreeGameRoundMatchRequest request
    );

    @Operation(
            summary = "자유게임 참가자 목록 조회",
            description = "자유게임 참가자 목록을 조회합니다. include=stats인 경우 매치 집계 정보를 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자유게임 참가자 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FreeGameParticipantsResponse.class)
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
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<FreeGameParticipantsResponse> getFreeGameParticipants(
            Long userId,
            Long gameId,
            @Parameter(description = "include=stats인 경우 매치 집계 정보를 포함합니다.")
            String include
    );

    @Operation(
            summary = "자유게임 참가자 상세 조회",
            description = "자유게임의 특정 참가자 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자유게임 참가자 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FreeGameParticipantDetailResponse.class)
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
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "자유게임 또는 참가자를 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<FreeGameParticipantDetailResponse> getFreeGameParticipantDetail(
            Long userId,
            Long gameId,
            Long participantId
    );
}
