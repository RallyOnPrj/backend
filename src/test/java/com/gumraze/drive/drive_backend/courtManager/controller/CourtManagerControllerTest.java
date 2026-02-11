package com.gumraze.drive.drive_backend.courtManager.controller;

import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.courtManager.constants.*;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    private RequestPostProcessor authenticatedUser(Long userId) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test()
    @DisplayName("최소 필수값으로 자유게임 생성 성공")
    void createFreeGame_success() throws Exception {
        // given: 최소 필수값만 포함된 생성 요청을 준비함.
        CreateFreeGameResponse response = CreateFreeGameResponse.builder()
                .gameId(101L).build();

        // 서비스 응답을 stub
        when(freeGameService.createFreeGame(anyLong(), any()))
                .thenReturn(response);

        // 토큰 검증을 stub
        // request 객체 생성
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // when: /free-games로 POST 요청을 보냄
        // then: 201과 응답 바디 구조/값이 일치하는지 확인
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(101));
    }

    @Test
    @DisplayName("자유게임 생성 시, title 누락")
    void createFreeGame_without_title() throws Exception {
        // given: 자유 게임 생성 시, title 누락
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title(null)
                .courtCount(2)
                .roundCount(3)
                .build();

        // when & then
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시, courtCount 누락")
    void createFreeGame_without_courtCount() throws Exception {
        // given: 자유게임 생성 시, courtCount 누락
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("테스트 게임")
                .roundCount(3)
                .build();

        // when & then: 자유게임 생성 호출 시 VALIDATION_ERROR 발생
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시, roundCount 누락 테스트")
    void createFreeGame_without_roundCount() throws Exception {
        // given: 자유게임 생성 시, roundCount 누락
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("테스트 게임")
                .courtCount(2)
                .build();

        // when & then: 자유게임 생성 호출 시 VALIDATION_ERROR 발생
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시, courtCount, roundCount 누락 테스트")
    void createFreeGame_without_courtCount_and_roundCount() throws Exception {
        // given: 자유게임 생성 시, courtCount, roundCount 누락
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .build();

        // when & then: 자유게임 생성 호출 시 VALIDATION_ERROR 발생
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시, participant가 존재할 때 최소 필수 항목이 존재하면 성공 테스트")
    void createFreeGame_with_participant() throws Exception {
        // given: 자유게임 생성 시, 참가자가 존재할 때 -> 최소 필드 값이 존재하면 성공
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .courtCount(2)
                .gradeType(GradeType.NATIONAL)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .gender(Gender.MALE)
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        CreateFreeGameResponse response = CreateFreeGameResponse.builder()
                .gameId(101L).build();
        when(freeGameService.createFreeGame(anyLong(), any()))
                .thenReturn(response);

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("자유게임 생성 시, participant가 존재할 때 최소 필수 항목 없는 경우 실패 테스트")
    void createFreeGame_with_participant_without_gender() throws Exception {
        // given: 참가자는 있지만, gender가 없는 경우
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .courtCount(2)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        // when & then
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시, 사용자 인증 누락 실패 테스트")
    void createFreeGame_without_token() throws Exception {
        // given: 참가자는 있지만, gender가 없는 경우
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .courtCount(2)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .gender(Gender.MALE)
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        String body = objectMapper.writeValueAsString(request);

        // 비어있는 토큰 설정
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, GradeType이 비어있으면 실패 테스트")
    void createFreeGame_without_gradeType() throws Exception {
        // given: 자유게임 생성 시 gradeType이 비어있을 경우
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(null)
                .courtCount(2)
                .roundCount(3)
                .build();

        // when & then: 자유게임 생성 시 400 에러 발생
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시, GradeType에 틀린 값이 입력되면 실패 테스트")
    void createFreeGame_with_invalid_gradeType() throws Exception {
        // given: GradeType에 틀린 값이 입력될 경우
        String body = """
                {
                  "title": "자유게임",
                  "gradeType": "REGION",
                  "courtCount": 2,
                  "roundCount": 3
                }
                """;

        // when & then: 자유게임 생성 시 400 에러 발생
        assertCreateFreeGameBadRequest(body);
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공 테스트")
    void getFreeGameDetail_success() throws Exception {
        // given
        Long userId = 99L;
        Long gameId = 1L;
        FreeGameDetailResponse response = buildFreeGameDetailResponse(userId, gameId);

        when(freeGameService.getFreeGameDetail(userId, gameId)).thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.title").value("자유게임"))
                .andExpect(jsonPath("$.gameType").value("FREE"))
                .andExpect(jsonPath("$.gameStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.matchRecordMode").value("STATUS_ONLY"))
                .andExpect(jsonPath("$.gradeType").value("NATIONAL"))
                .andExpect(jsonPath("$.courtCount").value(2))
                .andExpect(jsonPath("$.roundCount").value(2))
                .andExpect(jsonPath("$.organizerId").value(99))
        ;
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 인증 실패")
    void getFreeGameDetail_without_token() throws Exception {
        // given
        Long userId = 99L;
        Long gameId = 1L;
        FreeGameDetailResponse response = buildFreeGameDetailResponse(gameId, userId);

        when(freeGameService.getFreeGameDetail(userId, gameId)).thenReturn(response);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 존재하지 않는 gameId면 실패")
    void getFreeGameDetail_withUnknownGameId_returnError() throws Exception {
        // given
        Long userId = 99L;
        Long gameId = 1L;
        when(freeGameService.getFreeGameDetail(userId, gameId))
                .thenThrow(new NotFoundException("게임이 존재하지 않습니다."));

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andDo(print()) // 응답 로그 출력
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("게임이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공 테스트")
    void updateFreeGameInfo_success() throws Exception {
        // given: 정상적인 요청 입력
        Long gameId = 80L;
        Long userId = 1L;

        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();
        String body = objectMapper.writeValueAsString(request);
        UpdateFreeGameResponse response = UpdateFreeGameResponse.builder()
                .gameId(gameId)
                .build();

        // stub
        when(freeGameService.updateFreeGameInfo(anyLong(), anyLong(), any(UpdateFreeGameRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(80))
        ;
    }

    @Test
    @DisplayName("자유게임 수정 시, token 검증")
    void updateFreeGameInfo_without_token() throws Exception {
        // given
        Long gameId = 80L;
        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("자유게임 라운드/매치 조회 성공 테스트")
    void getFreeGameRoundMatch_success() throws Exception {
        // given
        Long userId = 1L;
        Long gameId = 2L;

        FreeGameMatchResponse match =
                FreeGameMatchResponse.builder()
                        .courtNumber(1L)
                        .teamAIds(List.of(201L, 202L))
                        .teamBIds(List.of(203L, 204L))
                        .matchStatus(MatchStatus.NOT_STARTED)
                        .matchResult(MatchResult.NULL)
                        .isActive(true)
                        .build();

        FreeGameRoundResponse round =
                FreeGameRoundResponse.builder()
                        .roundNumber(1)
                        .roundStatus(RoundStatus.NOT_STARTED)
                        .matches(List.of(match))
                        .build();

        FreeGameRoundMatchResponse response =
                FreeGameRoundMatchResponse.builder()
                        .gameId(gameId)
                        .rounds(List.of(round))
                        .build();

        when(freeGameService.getFreeGameRoundMatchResponse(userId, gameId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/rounds-and-matches", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(2L))
                .andExpect(jsonPath("$.rounds[0].roundNumber").value(1L))
                .andExpect(jsonPath("$.rounds[0].matches[0].courtNumber").value(1L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamAIds[0]").value(201L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamAIds[1]").value(202L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamBIds[0]").value(203L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamBIds[1]").value(204L))
                .andExpect(jsonPath("$.rounds[0].matches[0].matchStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.rounds[0].matches[0].matchResult").value("NULL"))
                .andExpect(jsonPath("$.rounds[0].matches[0].isActive").value(true))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 PATCH 성공")
    void updateRoundsAndMatches_patch_success() throws Exception {
        // given
        Long userId = 1L;
        Long gameId = 1L;

        when(freeGameService.updateFreeGameRoundMatch(anyLong(), anyLong(), any()))
                .thenReturn(new UpdateFreeGameRoundMatchResponse(gameId));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(101L, 102L))
                                                        .teamBIds(List.of(103L, 104L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("자유게임 참가자 목록 조회 성공 (include=stats)")
    void get_free_game_participants_with_stats_success() throws Exception {
        // given
        Long gameId = 101L;
        FreeGameParticipantResponse participant = buildParticipantResponse(201L, 10L, "KimA");
        FreeGameParticipantsResponse response =
                buildParticipantsResponse(gameId, List.of(withStats(participant, 3, 2, 1, 1)));

        when(freeGameService.getFreeGameParticipants(1L, gameId, true))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants", gameId)
                        .queryParam("include", "stats")
                        .with(authenticatedUser(1L))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(101))
                .andExpect(jsonPath("$.participants[0].assignedMatchCount").value(3))
                .andExpect(jsonPath("$.participants[0].completedMatchCount").value(2))
                .andExpect(jsonPath("$.participants[0].winCount").value(1))
                .andExpect(jsonPath("$.participants[0].lossCount").value(1));
    }

    @Test
    @DisplayName("자유게임 참가자 목록 조회 성공 (include 없음)")
    void get_free_game_participants_without_stats_success() throws Exception {
        // given
        Long gameId = 101L;
        FreeGameParticipantResponse participant = buildParticipantResponse(201L, 10L, "KimA");
        FreeGameParticipantsResponse response = buildParticipantsResponse(gameId, List.of(participant));

        when(freeGameService.getFreeGameParticipants(1L, gameId, false))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants", gameId)
                        .with(authenticatedUser(1L))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(101))
                .andExpect(jsonPath("$.participants[0].assignedMatchCount").doesNotExist())
                .andExpect(jsonPath("$.participants[0].completedMatchCount").doesNotExist())
                .andExpect(jsonPath("$.participants[0].winCount").doesNotExist())
                .andExpect(jsonPath("$.participants[0].lossCount").doesNotExist());
    }

    @Test
    @DisplayName("자유게임 참가자 상세 조회 성공")
    void get_free_game_participant_detail_success() throws Exception {
        // given
        Long userId = 1L;
        Long gameId = 101L;
        Long participantId = 201L;

        FreeGameParticipantDetailResponse response =
                buildParticipantDetailResponse(gameId, participantId, 10L, "KimA");

        when(freeGameService.getFreeGameParticipantDetail(userId, gameId, participantId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(101))
                .andExpect(jsonPath("$.participantId").value(201))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.displayName").value("KimA"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.ageGroup").value(30));
    }

    @Test
    @DisplayName("자유게임 참가자 상세 조회 시 존재하지 않는 participantId면 실패")
    void get_free_game_participant_detail_with_unknown_participant_then_not_found() throws Exception {
        // given
        Long userId = 1L;
        Long gameId = 101L;
        Long participantId = 999L;

        when(freeGameService.getFreeGameParticipantDetail(userId, gameId, participantId))
                .thenThrow(new NotFoundException("존재하지 않는 참가자입니다. participantId: " + participantId));

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("존재하지 않는 참가자입니다. participantId: " + participantId));
    }

    /*
     * Test Helper Method
     */
    private FreeGameDetailResponse buildFreeGameDetailResponse(Long organizerId, Long gameId) {
        return FreeGameDetailResponse.builder()
                .gameId(gameId)
                .title("자유게임")
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(2)
                .organizerId(organizerId)
                .build();
    }

    private void assertCreateFreeGameBadRequest(CreateFreeGameRequest request) throws Exception {
        assertCreateFreeGameBadRequest(objectMapper.writeValueAsString(request));
    }

    private void assertCreateFreeGameBadRequest(String body) throws Exception {
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    private FreeGameParticipantResponse buildParticipantResponse(Long participantId, Long userId, String displayName) {
        return FreeGameParticipantResponse.builder()
                .participantId(participantId)
                .userId(userId)
                .displayName(displayName)
                .gender(Gender.MALE)
                .grade(Grade.ROOKIE)
                .ageGroup(30)
                .build();
    }

    private FreeGameParticipantResponse withStats(
            FreeGameParticipantResponse participant,
            int assignedMatchCount,
            int completedMatchCount,
            int winCount,
            int lossCount
    ) {
        return FreeGameParticipantResponse.builder()
                .participantId(participant.getParticipantId())
                .userId(participant.getUserId())
                .displayName(participant.getDisplayName())
                .gender(participant.getGender())
                .grade(participant.getGrade())
                .ageGroup(participant.getAgeGroup())
                .assignedMatchCount(assignedMatchCount)
                .completedMatchCount(completedMatchCount)
                .winCount(winCount)
                .lossCount(lossCount)
                .build();
    }

    private FreeGameParticipantsResponse buildParticipantsResponse(
            Long gameId,
            List<FreeGameParticipantResponse> participants
    ) {
        return FreeGameParticipantsResponse.builder()
                .gameId(gameId)
                .matchRecordMode(MatchRecordMode.RESULT)
                .participants(participants)
                .build();
    }

    private FreeGameParticipantDetailResponse buildParticipantDetailResponse(
            Long gameId,
            Long participantId,
            Long userId,
            String displayName
    ) {
        return FreeGameParticipantDetailResponse.builder()
                .gameId(gameId)
                .participantId(participantId)
                .userId(userId)
                .displayName(displayName)
                .gender(Gender.MALE)
                .ageGroup(30)
                .build();
    }
}
