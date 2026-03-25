package com.gumraze.rallyon.backend.security.resourceserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiAudienceValidatorTest {

    private final ApiAudienceValidator validator = new ApiAudienceValidator("api.rallyon.test");

    @Test
    @DisplayName("예상한 audience가 있으면 access token을 허용한다")
    void validate_acceptsExpectedAudience() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .audience(List.of("api.rallyon.test"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    @DisplayName("예상한 audience가 없으면 access token을 거부한다")
    void validate_rejectsUnexpectedAudience() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .audience(List.of("another-api"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
