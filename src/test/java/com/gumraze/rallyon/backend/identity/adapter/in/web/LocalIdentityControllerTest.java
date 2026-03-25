package com.gumraze.rallyon.backend.identity.adapter.in.web;

import com.gumraze.rallyon.backend.authorization.config.AuthorizationHostSecurityConfig;
import com.gumraze.rallyon.backend.authorization.config.AuthorizationProperties;
import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.dto.RegisterLocalIdentityRequest;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalIdentityController.class)
@Import({
        SecurityConfig.class,
        AuthorizationHostSecurityConfig.class,
        LocalIdentityControllerTest.TestConfig.class
})
class LocalIdentityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RegisterLocalIdentityUseCase registerLocalIdentityUseCase;

    @Test
    @DisplayName("로컬 회원가입 계정 생성 API는 쿠키를 발급하지 않는다")
    void create_local_account_without_issuing_cookies() throws Exception {
        RegisterLocalIdentityRequest request = RegisterLocalIdentityRequest.builder()
                .email("user@rallyon.local")
                .password("password123!")
                .build();

        var result = mockMvc.perform(post("/identity/accounts/local")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andReturn();

        verify(registerLocalIdentityUseCase).register(any());
        assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        AuthorizationProperties authorizationProperties() {
            AuthorizationProperties properties = new AuthorizationProperties();
            properties.setIssuer("https://auth.rallyon.test");
            return properties;
        }
    }
}
