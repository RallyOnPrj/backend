package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.MatchRequest;
import com.gumraze.rallyon.backend.courtManager.dto.RoundRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import com.gumraze.rallyon.backend.courtManager.service.FreeGameService;
import com.gumraze.rallyon.backend.user.constants.GradeType;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 인증, 권한 관련 테스트
@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
public class CourtManagerControllerAuthTest {

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
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - organizer 아니면 403")
    void updateRoundMatch_when_not_organizer_then_forbidden() throws Exception {
        // given
        Long gameId = 1L;
        Long userId = 2L;       // organizer 아님

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
                .andExpect(jsonPath("$.title").exists())
        ;
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
        Long gameId = 1L;
        Long participantId = 10L;
        Long userId = 2L; // organizer 아님

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
    @DisplayName("shareCode로 공개 게임 조회 시, 토큰 없이 요청 가능")
    void getPublicFreeGameDetail_without_token_then_ok() throws Exception {
        // given
        String shareCode = "public-share-code";

        FreeGameDetailResponse response = FreeGameDetailResponse.builder()
                .gameId(1L)
                .title("공개 게임")
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
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
                .andExpect(jsonPath("$.gameId").value(1L))
                .andExpect(jsonPath("$.shareCode").value(shareCode))
                .andExpect(jsonPath("$.title").value("공개 게임"));
    }

    @Test
    @DisplayName("존재하지 않는 공유 링크면 404를 반환한다.")
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
