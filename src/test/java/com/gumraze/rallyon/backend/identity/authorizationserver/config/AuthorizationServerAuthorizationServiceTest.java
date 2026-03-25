package com.gumraze.rallyon.backend.identity.authorizationserver.config;

import com.gumraze.rallyon.backend.identity.authorizationserver.domain.IdentityAuthenticatedPrincipal;
import com.gumraze.rallyon.backend.user.constants.UserRole;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
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

class AuthorizationServerAuthorizationServiceTest {

    private EmbeddedDatabase database;

    @AfterEach
    void tearDown() {
        if (database != null) {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("authorization 저장소는 IdentityAuthenticatedPrincipal 속성을 round-trip 할 수 있다")
    void authorizationService_roundTripsIdentityAuthenticatedPrincipal() {
        database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:db/migration/V15__add_authorization_server_tables.sql")
                .build();

        JdbcOperations jdbcOperations = new JdbcTemplate(database);
        AuthorizationServerConfig config = new AuthorizationServerConfig();
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

        IdentityAuthenticatedPrincipal principal = new IdentityAuthenticatedPrincipal(
                UUID.randomUUID(),
                UserRole.USER,
                UserStatus.ACTIVE,
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
