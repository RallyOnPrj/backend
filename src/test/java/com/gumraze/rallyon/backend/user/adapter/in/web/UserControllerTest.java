package com.gumraze.rallyon.backend.user.adapter.in.web;

import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import com.gumraze.rallyon.backend.user.application.port.in.*;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.dto.UserProfileIdentityUpdateRequest;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
import com.gumraze.rallyon.backend.user.dto.UserSearchResponse;
import com.gumraze.rallyon.backend.user.dto.UserProfileUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private SearchUsersUseCase searchUsersUseCase;

    @MockitoBean
    private GetMyUserSummaryUseCase getMyUserSummaryUseCase;

    @MockitoBean
    private CreateMyProfileUseCase createMyProfileUseCase;

    @MockitoBean
    private GetMyProfilePrefillUseCase getMyProfilePrefillUseCase;

    @MockitoBean
    private GetMyProfileUseCase getMyProfileUseCase;

    @MockitoBean
    private UpdateMyProfileUseCase updateMyProfileUseCase;

    @MockitoBean
    private UpdateMyPublicIdentityUseCase updateMyPublicIdentityUseCase;

    @Test
    @DisplayName("PENDING 사용자가 /users/me 조회 시 status만 반환한다.")
    void get_me_returns_pending_user_status() throws Exception {
        UUID userId = UUID.randomUUID();
        UserMeResponse response = UserMeResponse.builder()
                .status(UserStatus.PENDING)
                .build();

        when(getMyUserSummaryUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me")
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(response.getStatus().name()))
                .andExpect(jsonPath("$.profileImageUrl").value(nullValue()))
                .andExpect(jsonPath("$.nickname").value(nullValue()));
    }

    @Test
    @DisplayName("ACTIVE 사용자가 /users/me 조회 시 프로필 정보를 반환한다.")
    void get_user_me_return_profile_when_active() throws Exception {
        UUID userId = UUID.randomUUID();
        UserMeResponse response = UserMeResponse.builder()
                .status(UserStatus.ACTIVE)
                .nickname("테스트 닉네임")
                .profileImageUrl("http://profile-image.com")
                .build();

        when(getMyUserSummaryUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me")
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(response.getStatus().name()))
                .andExpect(jsonPath("$.nickname").value("테스트 닉네임"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://profile-image.com"));
    }

    @Test
    @DisplayName("토큰이 없으면 401을 반환한다.")
    void get_user_me_returns_unauthorized_when_token_is_missing() throws Exception {
        mockMvc.perform(get("/users/me")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("닉네임으로 사용자 검색")
    void search_user_by_nickname() throws Exception {
        String nickname = "kim";
        UserSearchResponse response = UserSearchResponse.builder()
                .userId(UUID.randomUUID())
                .nickname(nickname)
                .tag("AB12")
                .profileImageUrl(null)
                .build();

        Page<UserSearchResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(searchUsersUseCase.search(any())).thenReturn(page);

        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("page", "0")
                        .param("size", "20")
                        .with(authenticatedUser(UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(response.getUserId().toString()))
                .andExpect(jsonPath("$.content[0].nickname").value(nickname))
                .andExpect(jsonPath("$.content[0].tag").value("AB12"));
    }

    @Test
    @DisplayName("닉네임과 태그로 사용자 검색")
    void search_user_by_nickname_and_tag() throws Exception {
        String nickname = "kim";
        String tag = "AB12";

        UserSearchResponse response = UserSearchResponse.builder()
                .userId(UUID.randomUUID())
                .nickname(nickname)
                .tag(tag)
                .profileImageUrl(null)
                .build();

        Page<UserSearchResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(searchUsersUseCase.search(any())).thenReturn(page);

        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("tag", tag)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tag").value(tag));
    }

    @Test
    @DisplayName("nickname 파라미터 누락 시 400에러 반환")
    void search_user_missing_nickname_returns_400() throws Exception {
        mockMvc.perform(get("/users")
                        .with(authenticatedUser(UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        verifyNoInteractions(searchUsersUseCase);
    }

    @Test
    @DisplayName("내 프로필 상세조회 성공 테스트")
    void get_my_profile_detail_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponseDto response = UserProfileResponseDto.builder()
                .status(UserStatus.ACTIVE)
                .nickname("테스트 닉네임")
                .tag("AB12")
                .profileImageUrl("http://profile-image.com")
                .birth(LocalDateTime.of(2000, 1, 1, 0, 0))
                .birthVisible(true)
                .gender(Gender.MALE)
                .regionalGrade(Grade.D)
                .nationalGrade(Grade.D)
                .districtName("테스트 구")
                .provinceName("테스트 시/도")
                .tagChangedAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                .createdAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                .build();

        when(getMyProfileUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me/profile")
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.nickname").value("테스트 닉네임"))
                .andExpect(jsonPath("$.tag").value("AB12"));
    }

    @Test
    @DisplayName("기본 프로필 수정 성공")
    void update_my_profile_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .birthVisible(true)
                .gender(Gender.MALE)
                .regionalGrade(Grade.D)
                .nationalGrade(Grade.D)
                .build();

        doNothing().when(updateMyProfileUseCase).update(any());

        mockMvc.perform(patch("/users/me/profile")
                        .with(authenticatedUser(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("닉네임/태그 변경 성공 테스트")
    void update_identity_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileIdentityUpdateRequest request = UserProfileIdentityUpdateRequest.builder()
                .nickname("newNickname")
                .tag("SON7")
                .build();

        doNothing().when(updateMyPublicIdentityUseCase).update(any());

        mockMvc.perform(patch("/users/me/profile/identity")
                        .with(authenticatedUser(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    private RequestPostProcessor authenticatedUser(UUID userId) {
        return authentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }
}
