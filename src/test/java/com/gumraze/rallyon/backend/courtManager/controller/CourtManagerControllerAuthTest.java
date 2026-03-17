package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import com.gumraze.rallyon.backend.courtManager.service.FreeGameService;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;

import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.authenticatedUser;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.freeGameDetailResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test
    @DisplayName("자유게임 생성 시 토큰이 없으면 401")
    void createFreeGame_without_token() throws Exception {
        // given: 정상 payload지만 인증 정보가 없는 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .gradeType(GradeType.NATIONAL)
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

        // when & then: 인증 정보가 없으면 401을 반환해야 한다.
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 토큰이 없으면 401")
    void getFreeGameDetail_without_token() throws Exception {
        // given: 인증되지 않은 요청으로 상세 조회를 호출한다.
        Long userId = 99L;
        UUID gameId = UUID.randomUUID();
        FreeGameDetailResponse response = freeGameDetailResponse(userId, gameId);

        when(freeGameService.getFreeGameDetail(userId, gameId)).thenReturn(response);

        // when & then: 인증 정보가 없으면 401을 반환해야 한다.
        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 시 토큰이 없으면 401")
    void updateFreeGameInfo_without_token() throws Exception {
        // given: 정상 payload지만 인증 정보가 없는 수정 요청을 준비한다.
        Long gameId = 80L;
        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then: 인증 정보가 없으면 401을 반환해야 한다.
        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 PATCH - 토큰 없음이면 401")
    void updateRoundMatch_without_token_then_unauthorized() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - organizer 아니면 403")
    void updateRoundMatch_when_not_organizer_then_forbidden() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        Long userId = 2L;

        when(freeGameService.updateFreeGameRoundMatch(eq(userId), eq(gameId), any()))
                .thenThrow(new ForbiddenException("Organizer 권한이 없습니다."));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()))
                        .build();

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("참가자 상세 조회 GET - 토큰 없음이면 401")
    void getParticipantDetail_without_token_then_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", 1L, 10L)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("참가자 상세 조회 - organizer 아니면 403")
    void getParticipantDetail_when_not_organizer_then_forbidden() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        Long userId = 2L;

        when(freeGameService.getFreeGameParticipantDetail(eq(userId), eq(gameId), eq(participantId)))
                .thenThrow(new ForbiddenException("Organizer 권한이 없습니다."));

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("shareCode로 공개 게임 조회 시 토큰 없이 요청 가능")
    void getPublicFreeGameDetail_without_token_then_ok() throws Exception {
        // given
        String shareCode = "public-share-code";

        FreeGameDetailResponse response = FreeGameDetailResponse.builder()
                .gameId(UUID.randomUUID())
                .title("공개 게임")
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
                .location("잠실 배드민턴장")
                .courtCount(2)
                .roundCount(3)
                .organizerId(10L)
                .shareCode(shareCode)
                .build();

        when(freeGameService.getPublicFreeGameDetail(shareCode))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/share/{shareCode}", shareCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(response.getGameId().toString()))
                .andExpect(jsonPath("$.shareCode").value(shareCode))
                .andExpect(jsonPath("$.title").value("공개 게임"));
    }

    @Test
    @DisplayName("존재하지 않는 공유 링크면 404를 반환한다")
    void getPublicFreeGameDetail_when_shareCode_not_found_then_not_found() throws Exception {
        // given
        String shareCode = "missing-share-code";

        when(freeGameService.getPublicFreeGameDetail(shareCode))
                .thenThrow(new NotFoundException("존재하지 않는 공유 링크입니다."));

        // when & then
        mockMvc.perform(get("/free-games/share/{shareCode}", shareCode)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound());
    }

}
