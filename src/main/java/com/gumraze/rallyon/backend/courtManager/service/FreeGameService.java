package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.courtManager.dto.*;
import java.util.UUID;

/**
 * 자유게임 관련 주요 유스케이스를 제공한다.
 *
 * <p>게임 생성, 상세 조회, 라운드/매치 수정, 참가자 조회와 같은
 * 코트 매니저 기능의 서비스 계약을 정의한다.</p>
 */
public interface FreeGameService {

    /**
     * 새로운 자유게임을 생성한다.
     *
     * @param userId 게임 생성 요청 사용자 ID
     * @param request 자유게임 생성 요청 정보
     * @return 생성된 자유게임 식별 정보
     * @throws IllegalArgumentException 요청 값이 유효하지 않거나 생성자가 존재하지 않는 경우
     */
    CreateFreeGameResponse createFreeGame(Long userId, CreateFreeGameRequest request);

    /**
     * 자유게임 상세 정보를 조회한다.
     *
     * @param userId 조회 요청 사용자 ID
     * @param gameId 자유게임 ID
     * @return 자유게임 상세 정보
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException 게임 또는 게임 설정이 없는 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.ForbiddenException 요청자가 게임 생성자가 아닌 경우
     */
    FreeGameDetailResponse getFreeGameDetail(Long userId, UUID gameId);

    /**
     * 자유게임의 기본 정보를 수정한다.
     *
     * @param userId 수정 요청 사용자 ID
     * @param gameId 자유게임 ID
     * @param request 자유게임 수정 요청 정보
     * @return 수정된 자유게임 기본 정보
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException 게임이 존재하지 않는 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.ForbiddenException 요청자가 게임 생성자가 아닌 경우
     */
    UpdateFreeGameResponse updateFreeGameInfo(Long userId, UUID gameId, UpdateFreeGameRequest request);

    /**
     * 자유게임의 라운드 및 매치 정보를 조회한다.
     *
     * @param userId 조회 요청 사용자 ID
     * @param gameId 자유게임 ID
     * @return 라운드 및 매치 정보
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException 게임이 존재하지 않는 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.ForbiddenException 요청자가 게임 생성자가 아닌 경우
     */
    FreeGameRoundMatchResponse getFreeGameRoundMatchResponse(Long userId, UUID gameId);

    /**
     * 자유게임의 라운드 및 매치 정보를 수정한다.
     *
     * @param userId 수정 요청 사용자 ID
     * @param gameId 자유게임 ID
     * @param request 라운드 및 매치 수정 요청 정보
     * @return 수정 결과 정보
     * @throws IllegalArgumentException 요청 값이 유효하지 않은 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException 게임이 존재하지 않는 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.ForbiddenException 요청자가 게임 생성자가 아닌 경우
     */
    UpdateFreeGameRoundMatchResponse updateFreeGameRoundMatch(Long userId, UUID gameId, UpdateFreeGameRoundMatchRequest request);

    /**
     * 자유게임 참가자 목록을 조회한다.
     *
     * @param userId 조회 요청 사용자 ID
     * @param gameId 자유게임 ID
     * @param includeStats 참가자별 경기 통계 포함 여부
     * @return 참가자 목록 정보
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException 게임이 존재하지 않는 경우
     * @throws com.gumraze.drive.drive_backend.common.exception.ForbiddenException 요청자가 게임 생성자가 아닌 경우
     */
    FreeGameParticipantsResponse getFreeGameParticipants(Long userId, UUID gameId, boolean includeStats);

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
     * @throws com.gumraze.rallyon.backend.common.exception.NotFoundException game/participant가 없거나 다른 게임 소속인 경우
     * @throws com.gumraze.rallyon.backend.common.exception.ForbiddenException 요청자가 organizer가 아닌 경우
     */
    FreeGameParticipantDetailResponse getFreeGameParticipantDetail(Long userId, UUID gameId, UUID participantId);

    /**
     * shareCode로 공개 가능한 자유게임 상세 정보를 조회한다.
     *
     * @param shareCode 외부 공유 링크에 사용되는 공개 식별자
     * @return 공개 조회용 자유게임 상세 정보
     * @throws com.gumraze.drive.drive_backend.common.exception.NotFoundException shareCode에 해당하는 게임 또는 게임 설정이 없는 경우
     */
    FreeGameDetailResponse getPublicFreeGameDetail(String shareCode);
}
