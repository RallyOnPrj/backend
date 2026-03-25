package com.gumraze.rallyon.backend.authorization.config;

import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.identity.domain.IdentityRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationPersistenceTest {

    private EmbeddedDatabase database;

    @AfterEach
    void tearDown() {
        if (database != null) {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("authorization 저장소는 AuthenticatedIdentity 속성을 round-trip 할 수 있다")
    void authorizationService_roundTripsAuthenticatedIdentity() {
        database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:db/migration/V1__init_schema.sql")
                .build();

        JdbcOperations jdbcOperations = new JdbcTemplate(database);
        AuthorizationConfig config = new AuthorizationConfig();
        RegisteredClientRepository registeredClientRepository = config.registeredClientRepository(jdbcOperations);
        OAuth2AuthorizationService authorizationService =
                config.authorizationService(jdbcOperations, registeredClientRepository);

        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("rallyon-web")
                .clientName("RallyOn Web")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://auth.rallyon.test/identity/session/callback")
                .scope("openid")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder().build())
                .build();
        registeredClientRepository.save(registeredClient);

        AuthenticatedIdentity principal = new AuthenticatedIdentity(
                UUID.randomUUID(),
                IdentityRole.USER,
                "tester"
        );

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(UUID.randomUUID().toString())
                .principalName(principal.getName())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .attribute(Principal.class.getName(), principal)
                .attribute("identity_principal", principal)
                .token(new OAuth2AuthorizationCode(
                        "authorization-code",
                        Instant.now(),
                        Instant.now().plusSeconds(300)
                ))
                .build();

        authorizationService.save(authorization);

        OAuth2Authorization loaded = authorizationService.findById(authorization.getId());

        assertThat(loaded).isNotNull();
        assertThat((Object) loaded.getAttribute(Principal.class.getName())).isEqualTo(principal);
        assertThat((Object) loaded.getAttribute("identity_principal")).isEqualTo(principal);
    }
}
