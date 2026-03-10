package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.dto.*;

public interface FreeGameService {
    CreateFreeGameResponse createFreeGame(Long userId, CreateFreeGameRequest request);

    FreeGameDetailResponse getFreeGameDetail(Long userId, Long gameId);

    UpdateFreeGameResponse updateFreeGameInfo(Long userId, Long gameId, UpdateFreeGameRequest request);

    FreeGameRoundMatchResponse getFreeGameRoundMatchResponse(Long userId, Long gameId);

    UpdateFreeGameRoundMatchResponse updateFreeGameRoundMatch(Long userId, Long gameId, UpdateFreeGameRoundMatchRequest request);

    FreeGameParticipantsResponse getFreeGameParticipants(Long userId, Long gameId, boolean includeStats);

    /**
     * 자유게임 참가자 상세 정보를 조회한다.
     *
     * <p>요청자는 반드시 해당 게임의 organizer여야 하며,
     * participantId가 존재하고 해당 gameId에 속해 있어야 한다.</p>
     *
     * @param userId 조회 요청 사용자 ID
     * @param gameId 자유게임 ID
     * @param participantId 참가자 ID
     * @return 참가자 상세 정보
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException game/participant가 없거나 다른 게임 소속인 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.ForbiddenException 요청자가 organizer가 아닌 경우
     */
    FreeGameParticipantDetailResponse getFreeGameParticipantDetail(Long userId, Long gameId, Long participantId);

    FreeGameDetailResponse getPublicFreeGameDetail(String shareCode);
}
