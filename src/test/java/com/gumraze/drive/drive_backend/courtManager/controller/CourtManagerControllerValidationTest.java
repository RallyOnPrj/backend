package com.gumraze.drive.drive_backend.courtManager.controller;

import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.courtManager.dto.MatchRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.RoundRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 요청 값 검증 test
@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
public class CourtManagerControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @BeforeEach
    void setUp() {
        when(jwtAccessTokenValidator.validateAndGetUserId("token")).thenReturn(Optional.of(1L));
    }

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
    @DisplayName("라운드/매치 부분 수정 - rounds 누락이면 400")
    void updateRoundMatch_without_rounds_then_bad_request() throws Exception {
        // given
        // 라운드 누락
        String body = "{}";

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - roundNumber 누락이면 400")
    void updateRoundMatch_without_roundNumber_then_bad_request() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(null)  // 누락
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

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - matches 누락이면 400")
    void updateRoundMatch_without_matches_then_bad_request() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(null)  // match 누락
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - teamAIds 누락이면 400")
    void updateRoundMatch_without_teamAIds_then_bad_request() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(null)
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - teamBIds 누락이면 400")
    void updateRoundMatch_without_teamBIds_then_bad_request() throws Exception {
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
                                                        .teamBIds(null)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
        ;
    }

}
