package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.constants.*;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import com.gumraze.rallyon.backend.courtManager.entity.*;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FreeGameServiceImpl implements FreeGameService {

    private final GameRepository gameRepository;
    private final GameParticipantRepository gameParticipantRepository;
    private final FreeGameSettingRepository freeGameSettingRepository;
    private final FreeGameRoundRepository freeGameRoundRepository;
    private final FreeGameMatchRepository freeGameMatchRepository;

    @Override
    @Transactional(readOnly = true)
    public FreeGameDetailResponse getFreeGameDetail(UUID userId, UUID gameId) {
        FreeGame freeGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임입니다. gameId: " + gameId));

        // 생성자만 조회 가능
        if (!freeGame.getOrganizer().getId().equals(userId)) {
            throw new ForbiddenException("게임 생성자만 조회할 수 있습니다.");
        }

        FreeGameSetting setting = freeGameSettingRepository.findByFreeGameId(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임 세팅입니다. gameId: " + gameId));

        return FreeGameDetailResponse.from(freeGame, setting);
    }

    @Override
    @Transactional
    public UpdateFreeGameResponse updateFreeGameInfo(UUID userId, UUID gameId, UpdateFreeGameRequest request) {
        FreeGame freeGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임입니다. gameId: " + gameId));

        if (!freeGame.getOrganizer().getId().equals(userId)) {
            throw new ForbiddenException("게임 생성자만 수정할 수 있습니다.");
        }

        // manager 제외
        if (request.getManagerIds() != null) {
            throw new UnsupportedOperationException("매니저 수정 기능은 현재 미개발 상태입니다.");
        }
        // update 수행
        freeGame.update(
                request.getTitle(),
                request.getMatchRecordMode(),
                request.getGradeType(),
                request.getLocation()
        );
        gameRepository.save(freeGame);
        return UpdateFreeGameResponse.from(freeGame);
    }

    @Override
    @Transactional(readOnly = true)
    public FreeGameRoundMatchResponse getFreeGameRoundMatchResponse(UUID userId, UUID gameId) {
        // gameId로 Game 조회
        FreeGame freeGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임입니다. gameId: " + gameId));

        if (!freeGame.getOrganizer().getId().equals(userId)) {
            throw new ForbiddenException("게임의 주최자만 접근할 수 있습니다.");
        }

        // round 조회
        List<FreeGameRound> rounds = freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId);

        // round가 존재하지 않으면 빈 배열로 반환
        if (rounds.isEmpty()) {
            return FreeGameRoundMatchResponse.builder()
                    .gameId(gameId)
                    .rounds(List.of())
                    .build();
        }

        // match 조회
        List<UUID> roundIds = rounds.stream()
                .map(FreeGameRound::getId)
                .toList();
        List<FreeGameMatch> matches = freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(roundIds);

        // match를 roundID 기준으로 그룹화
        Map<UUID, List<FreeGameMatch>> matchesByRoundId = matches.stream()
                .collect(Collectors.groupingBy(m -> m.getRound().getId()));

        // DTO
        List<FreeGameRoundResponse> roundResponses = rounds.stream()
                .map(round -> {
                    List<FreeGameMatch> roundMatches = matchesByRoundId.getOrDefault(round.getId(), List.of());
                    List<FreeGameMatchResponse> matchResponses = roundMatches.stream()
                            .map(match -> FreeGameMatchResponse.builder()
                                    .courtNumber(match.getCourtNumber().longValue())
                                    .teamAIds(Arrays.asList(
                                            match.getTeamAPlayer1() != null ? match.getTeamAPlayer1().getId() : null,
                                            match.getTeamAPlayer2() != null ? match.getTeamAPlayer2().getId() : null
                                            ))
                                    .teamBIds(Arrays.asList(
                                            match.getTeamBPlayer1() != null ? match.getTeamBPlayer1().getId() : null,
                                            match.getTeamBPlayer2() != null ? match.getTeamBPlayer2().getId() : null
                                            ))
                                    .matchStatus(match.getMatchStatus())
                                    .matchResult(match.getMatchResult() != null ? match.getMatchResult() : MatchResult.NULL)
                                    .isActive(match.getIsActive())
                                    .build())
                            .toList();
                    return FreeGameRoundResponse.builder()
                            .roundNumber(round.getRoundNumber())
                            .roundStatus(round.getRoundStatus())
                            .matches(matchResponses)
                            .build();
                })
                .toList();
        return FreeGameRoundMatchResponse.builder()
                .gameId(gameId)
                .rounds(roundResponses)
                .build();
    }

    @Override
    @Transactional
    public UpdateFreeGameRoundMatchResponse updateFreeGameRoundMatch(
            UUID userId,
            UUID gameId,
            UpdateFreeGameRoundMatchRequest request
    ) {

        // 게임 id와 organizer id 검증 수행
        FreeGame freeGame = validateGameAndOrganizer(gameId, userId);

        // 게임 상태 검증
        if (freeGame.getGameStatus() == GameStatus.COMPLETED) {
            throw new IllegalArgumentException("게임 상태가 COMPLETED이므로 수정이 불가합니다.");
        }

        // 기존 라운드 조회
        List<FreeGameRound> existingRounds = freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId);

        // roundNumber를 FreeGameRound로 매핑
        Map<Integer, FreeGameRound> roundMap =
                existingRounds.stream()
                        .collect(Collectors.toMap(
                                FreeGameRound::getRoundNumber,
                                r -> r
                        ));

        if (request == null || request.getRounds() == null) {
            return UpdateFreeGameRoundMatchResponse.from(gameId);
        }

        // 게임 참가자 조회
        Map<UUID, GameParticipant> participantsById = gameParticipantRepository.findByFreeGameId(gameId).stream()
                .collect(Collectors.toMap(GameParticipant::getId, participant -> participant));
        Set<UUID> participantIdsInGame = participantsById.keySet();

        // 요청 라운드 처리
        for (RoundRequest roundRequest : request.getRounds()) {

            // 요청한 roundNumber
            Integer requestedRoundNumber = roundRequest.getRoundNumber();

            // roundNumber 필수
            if (requestedRoundNumber == null) {
                throw new IllegalArgumentException("roundNumber는 필수입니다.");
            }

            // round에는 반드시 match가 있어야함
            if (roundRequest.getMatches() == null || roundRequest.getMatches().isEmpty()) {
                throw new IllegalArgumentException("라운드는 최소 1개의 매치를 포함해야합니다.");
            }

            // round 내 중복 참가자 검증용
            Set<UUID> usedParticipantIds = new HashSet<>();

            // teamAIds, teamBIds 검증
            for (MatchRequest matchRequest : roundRequest.getMatches()) {
                List<UUID> teamAIds = matchRequest.getTeamAIds();
                List<UUID> teamBIds = matchRequest.getTeamBIds();

                if (teamAIds == null || teamBIds == null) {
                    throw new IllegalArgumentException("teamAIds와 teamBIds는 모두 필수입니다.");
                }
                if (teamAIds.size() != 2 || teamBIds.size() != 2) {
                    throw new IllegalArgumentException("teamAIds와 teamBIds의 길이는 2여야 합니다.");
                }

                // 매치 내 중복 participantId 검증
                Set<UUID> matchParticipantIds = new HashSet<>();

                for (UUID id : teamAIds) {
                    if (id == null) {
                        continue;
                    }
                    if (!participantIdsInGame.contains(id)) {
                        throw new IllegalArgumentException("존재하지 않거나 해당 게임에 속하지 않는 participantId입니다. participantId: " + id);
                    }
                    if (!matchParticipantIds.add(id)) {
                        throw new IllegalArgumentException("match 내 participantId 중복입니다. participantId: " + id);
                    }
                    if (!usedParticipantIds.add(id)) {
                        throw new IllegalArgumentException("round 내 participantId 중복입니다. participantId: " + id);
                    }
                }

                for (UUID id : teamBIds) {
                    if (id == null) {
                        continue;
                    }
                    if (!participantIdsInGame.contains(id)) {
                        throw new IllegalArgumentException("존재하지 않거나 해당 게임에 속하지 않는 participantId입니다. participantId: " + id);
                    }
                    if (!matchParticipantIds.add(id)) {
                        throw new IllegalArgumentException("match 내 participantId 중복입니다. participantId: " + id);
                    }
                    if (!usedParticipantIds.add(id)) {
                        throw new IllegalArgumentException("round 내 participantId 중복입니다. participantId: " + id);
                    }
                }
            }

            // courtNumber가 1..n 연속인지 검증
            List<Integer> courtNumbers = roundRequest.getMatches().stream()
                    .map(MatchRequest::getCourtNumber)
                    .toList();

            // courtNumber 최소값 검증
            if (courtNumbers.stream().anyMatch(n -> n == null || n < 1)) {
                throw new IllegalArgumentException("courtNumber는 1이상이어야 합니다.");
            }

            List<Integer> sorted = courtNumbers.stream().sorted().toList();
            for (int i = 0; i < sorted.size(); i++) {
                if (sorted.get(i) != i + 1) {
                    throw new IllegalArgumentException("courtNumber는 1..n 연속이어야 합니다.");
                }
            }

            // round의 courtNumber는 서로 다른 값을 가지고 있어야함.
            long distinctCount = courtNumbers.stream().distinct().count();
            if (distinctCount != courtNumbers.size()) {
                throw new IllegalArgumentException("매치는 서로 다른 courtNumber를 가져야합니다.");
            }

            // round 결정
            FreeGameRound resolvedRound = roundMap.get(requestedRoundNumber);
            if (resolvedRound == null) {
                // 신규 라운드인 경우만 라운드 생성
                resolvedRound = FreeGameRound.builder()
                        .freeGame(freeGame)
                        .roundNumber(roundRequest.getRoundNumber())
                        .roundStatus(RoundStatus.NOT_STARTED)
                        .build();
                resolvedRound = freeGameRoundRepository.save(resolvedRound);
            }

            // 기존 매치 삭제
            if (resolvedRound.getId() != null) {
                freeGameMatchRepository.deleteByRoundId(resolvedRound.getId());
            }

            // 새 매치 생성
            final FreeGameRound targetRound = resolvedRound;
            List<FreeGameMatch> newMatches = roundRequest.getMatches().stream()
                    .map(matchRequest -> {
                        List<UUID> teamAIds = matchRequest.getTeamAIds();
                        List<UUID> teamBIds = matchRequest.getTeamBIds();

                        return FreeGameMatch.builder()
                                .round(targetRound)
                                .courtNumber(matchRequest.getCourtNumber())
                                .teamAPlayer1(resolveParticipant(participantsById, teamAIds.get(0)))
                                .teamAPlayer2(resolveParticipant(participantsById, teamAIds.get(1)))
                                .teamBPlayer1(resolveParticipant(participantsById, teamBIds.get(0)))
                                .teamBPlayer2(resolveParticipant(participantsById, teamBIds.get(1)))
                                .build();
                    })
                    .toList();
            // 매치 전체 교체 저장
            freeGameMatchRepository.saveAll(newMatches);
        }
        return UpdateFreeGameRoundMatchResponse.from(gameId);
    }

    private GameParticipant resolveParticipant(Map<UUID, GameParticipant> participantsById, UUID participantId) {
        if (participantId == null) {
            return null;
        }
        return participantsById.get(participantId);
    }

    @Override
    @Transactional(readOnly = true)
    public FreeGameParticipantsResponse getFreeGameParticipants(
            UUID userId,
            UUID gameId,
            boolean includeStats
    ) {
        // organizer 권한 검증 및 게임 조회
        FreeGame freeGame = validateGameAndOrganizer(gameId, userId);

        // 참가자 기본 목록은 생성 순서 기준으로 정렬
        List<GameParticipant> participants = gameParticipantRepository.findByFreeGameId(gameId).stream()
                .sorted(Comparator
                        .comparing(
                                GameParticipant::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                GameParticipant::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                .toList();

        MatchRecordMode matchRecordMode = freeGame.getMatchRecordMode();

        // stats 미요청 또는 참가자 없음: 기본 정보만 반환
        if (!includeStats || participants.isEmpty()) {
            List<FreeGameParticipantResponse> participantResponses = participants.stream()
                    .map(participant -> FreeGameParticipantResponse.builder()
                            .participantId(participant.getId())
                            .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                            .displayName(participant.getDisplayName())
                            .gender(participant.getGender())
                            .grade(participant.getGrade())
                            .ageGroup(participant.getAgeGroup())
                            .build())
                    .toList();

            return FreeGameParticipantsResponse.builder()
                    .gameId(gameId)
                    .matchRecordMode(matchRecordMode)
                    .participants(participantResponses)
                    .build();
        }

        // 참가자별 통계 초기화
        Map<UUID, ParticipantStats> statsByParticipantId = new HashMap<>();
        for (GameParticipant participant : participants) {
            statsByParticipantId.put(participant.getId(), new ParticipantStats());
        }

        List<FreeGameRound> rounds = freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId);
        if (!rounds.isEmpty()) {
            List<UUID> roundIds = rounds.stream()
                    .map(FreeGameRound::getId)
                    .toList();
            List<FreeGameMatch> matches = freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(roundIds);

            for (FreeGameMatch match : matches) {
                // 매치에 배정된 참가자 ID 수집
                Set<UUID> matchParticipantIds = new HashSet<>();
                addParticipantId(matchParticipantIds, match.getTeamAPlayer1());
                addParticipantId(matchParticipantIds, match.getTeamAPlayer2());
                addParticipantId(matchParticipantIds, match.getTeamBPlayer1());
                addParticipantId(matchParticipantIds, match.getTeamBPlayer2());

                // 배정된 매치 수 카운트
                for (UUID participantId : matchParticipantIds) {
                    ParticipantStats stats = statsByParticipantId.get(participantId);
                    if (stats != null) {
                        stats.assignedMatchCount++;
                    }
                }

                // 완료된 매치 수 카운트
                if (match.getMatchStatus() == MatchStatus.COMPLETED) {
                    for (UUID participantId : matchParticipantIds) {
                        ParticipantStats stats = statsByParticipantId.get(participantId);
                        if (stats != null) {
                            stats.completedMatchCount++;
                        }
                    }
                }

                // RESULT 모드일 때만 승/패 집계
                if (matchRecordMode == MatchRecordMode.RESULT) {
                    applyWinLossCounts(match, statsByParticipantId);
                }
            }
        }

        List<FreeGameParticipantResponse> participantResponses = participants.stream()
                .map(participant -> {
                    ParticipantStats stats = statsByParticipantId.get(participant.getId());
                    Integer winCount = matchRecordMode == MatchRecordMode.RESULT ? stats.winCount : null;
                    Integer lossCount = matchRecordMode == MatchRecordMode.RESULT ? stats.lossCount : null;

                    return FreeGameParticipantResponse.builder()
                            .participantId(participant.getId())
                            .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                            .displayName(participant.getDisplayName())
                            .gender(participant.getGender())
                            .grade(participant.getGrade())
                            .ageGroup(participant.getAgeGroup())
                            .assignedMatchCount(stats.assignedMatchCount)
                            .completedMatchCount(stats.completedMatchCount)
                            .winCount(winCount)
                            .lossCount(lossCount)
                            .build();
                })
                .toList();

        return FreeGameParticipantsResponse.builder()
                .gameId(gameId)
                .matchRecordMode(matchRecordMode)
                .participants(participantResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FreeGameParticipantDetailResponse getFreeGameParticipantDetail(
            UUID userId,
            UUID gameId,
            UUID participantId
    ) {
        // 게임 존재 및 권한 체크 수행
        validateGameAndOrganizer(gameId, userId);

        // 참가자 조회
        GameParticipant participant = gameParticipantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 참가자입니다. participantId: " + participantId));

        if (!participant.getFreeGame().getId().equals(gameId)) {
            throw new NotFoundException("참가자가 다른 게임에 속해 있습니다. participantId: " + participantId);
        }

        return FreeGameParticipantDetailResponse.builder()
                .gameId(gameId)
                .participantId(participant.getId())
                .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                .displayName(participant.getDisplayName())
                .gender(participant.getGender())
                .grade(participant.getGrade())
                .ageGroup(participant.getAgeGroup())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FreeGameDetailResponse getPublicFreeGameDetail(String shareCode) {
        FreeGame freeGame = gameRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 공유 링크입니다. shareCode: " + shareCode));
        FreeGameSetting setting = freeGameSettingRepository.findByFreeGameId(freeGame.getId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임 세팅입니다. gameId: " + freeGame.getId()));

        return FreeGameDetailResponse.from(freeGame, setting);
    }

    private FreeGame validateGameAndOrganizer(UUID gameId, UUID userId) {
        FreeGame freeGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임입니다. gameId: " + gameId));

        if (!freeGame.getOrganizer().getId().equals(userId)) {
            throw new ForbiddenException("게임의 organizer가 아닙니다. gameId: " + gameId);
        }

        return freeGame;
    }

    private void addParticipantId(Set<UUID> target, GameParticipant participant) {
        // null 참가자는 무시
        if (participant != null) {
            target.add(participant.getId());
        }
    }

    private void applyWinLossCounts(
            FreeGameMatch match,
            Map<UUID, ParticipantStats> statsByParticipantId
    ) {
        // 승/패가 확정된 매치만 집계
        MatchResult result = match.getMatchResult();
        if (result != MatchResult.TEAM_A_WIN && result != MatchResult.TEAM_B_WIN) {
            return;
        }

        // 팀별 참가자 ID 구성
        Set<UUID> teamAIds = new HashSet<>();
        addParticipantId(teamAIds, match.getTeamAPlayer1());
        addParticipantId(teamAIds, match.getTeamAPlayer2());

        Set<UUID> teamBIds = new HashSet<>();
        addParticipantId(teamBIds, match.getTeamBPlayer1());
        addParticipantId(teamBIds, match.getTeamBPlayer2());

        if (result == MatchResult.TEAM_A_WIN) {
            for (UUID participantId : teamAIds) {
                ParticipantStats stats = statsByParticipantId.get(participantId);
                if (stats != null) {
                    stats.winCount++;
                }
            }
            for (UUID participantId : teamBIds) {
                ParticipantStats stats = statsByParticipantId.get(participantId);
                if (stats != null) {
                    stats.lossCount++;
                }
            }
            return;
        }

        for (UUID participantId : teamBIds) {
            ParticipantStats stats = statsByParticipantId.get(participantId);
            if (stats != null) {
                stats.winCount++;
            }
        }
        for (UUID participantId : teamAIds) {
            ParticipantStats stats = statsByParticipantId.get(participantId);
            if (stats != null) {
                stats.lossCount++;
            }
        }
    }

    private static class ParticipantStats {
        private int assignedMatchCount;
        private int completedMatchCount;
        private int winCount;
        private int lossCount;
    }
}
