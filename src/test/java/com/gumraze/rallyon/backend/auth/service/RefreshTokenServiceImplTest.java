package com.gumraze.rallyon.backend.auth.service;

import com.gumraze.rallyon.backend.auth.entity.RefreshToken;
import com.gumraze.rallyon.backend.auth.repository.RefreshTokenRepository;
import com.gumraze.rallyon.backend.auth.token.JwtProperties;
import com.gumraze.rallyon.backend.auth.token.RefreshTokenGenerator;
import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RefreshTokenServiceImplTest {

    private RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenGenerator refreshTokenGenerator;
    private UserRepository userRepository;
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        refreshTokenGenerator = new RefreshTokenGenerator();
        userRepository = mock(UserRepository.class);
        JwtProperties properties = new JwtProperties(
                new JwtProperties.AccessToken("test-secret-key-test-secret-key", 1_800_000L),
                new JwtProperties.RefreshToken(12L)
        );
        refreshTokenService = new RefreshTokenServiceImpl(
                refreshTokenRepository,
                refreshTokenGenerator,
                userRepository,
                properties
        );
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이면 연결된 사용자 식별자를 반환한다")
    void validateAndGetUserId_returnsUserId_whenTokenIsValid() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        String plainToken = "valid-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenGenerator.hash(plainToken),
                LocalDateTime.now().plusHours(1)
        );

        when(refreshTokenRepository.findByTokenHash(refreshTokenGenerator.hash(plainToken)))
                .thenReturn(Optional.of(refreshToken));

        // when
        UUID result = refreshTokenService.validateAndGetUserId(plainToken);

        // then
        assertThat(result).isEqualTo(userId);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰이면 401 예외를 던진다")
    void validateAndGetUserId_throwsUnauthorized_whenTokenDoesNotExist() {
        // given
        String plainToken = "missing-refresh-token";
        when(refreshTokenRepository.findByTokenHash(refreshTokenGenerator.hash(plainToken)))
                .thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> refreshTokenService.validateAndGetUserId(plainToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("유효하지 않은 Refresh Token입니다.");
    }

    @Test
    @DisplayName("만료된 리프레시 토큰이면 삭제 후 401 예외를 던진다")
    void validateAndGetUserId_deletesExpiredToken_andThrowsUnauthorized() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        String plainToken = "expired-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenGenerator.hash(plainToken),
                LocalDateTime.now().minusMinutes(1)
        );

        when(refreshTokenRepository.findByTokenHash(refreshTokenGenerator.hash(plainToken)))
                .thenReturn(Optional.of(refreshToken));

        // when/then
        assertThatThrownBy(() -> refreshTokenService.validateAndGetUserId(plainToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("만료된 Refresh Token입니다.");

        verify(refreshTokenRepository).delete(refreshToken);
    }
}
