package com.gumraze.rallyon.backend.authorization.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.security.resourceserver.ApiAudienceValidator;
import com.gumraze.rallyon.backend.security.resourceserver.ResourceServerProperties;
import com.gumraze.rallyon.backend.security.web.HostRequestMatchers;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties(AuthorizationProperties.class)
public class AuthorizationConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            AuthorizationProperties properties,
            AuthorizationServerSettings authorizationServerSettings,
            RegisteredClientRepository registeredClientRepository,
            OAuth2AuthorizationService authorizationService,
            OAuth2AuthorizationConsentService authorizationConsentService
    ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(new AndRequestMatcher(
                        HostRequestMatchers.forConfiguredHost(extractHost(properties.getIssuer())),
                        authorizationServerConfigurer.getEndpointsMatcher()
                ))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .with(authorizationServerConfigurer, authorizationServer -> authorizationServer
                        .registeredClientRepository(registeredClientRepository)
                        .authorizationService(authorizationService)
                        .authorizationConsentService(authorizationConsentService)
                        .authorizationServerSettings(authorizationServerSettings)
                        .oidc(Customizer.withDefaults())
                )
                .exceptionHandling(exceptionHandlingLoginEntryPoint())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    private Customizer<ExceptionHandlingConfigurer<HttpSecurity>> exceptionHandlingLoginEntryPoint() {
        return exception -> exception.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"),
                new MediaTypeRequestMatcher(org.springframework.http.MediaType.TEXT_HTML)
        );
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations) {
        return new JdbcRegisteredClientRepository(jdbcOperations);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository registeredClientRepository
    ) {
        JsonMapper authorizationJsonMapper = authorizationJsonMapper();
        JdbcOAuth2AuthorizationService authorizationService =
                new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
        authorizationService.setAuthorizationRowMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(
                        registeredClientRepository,
                        authorizationJsonMapper
                )
        );
        authorizationService.setAuthorizationParametersMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper(
                        authorizationJsonMapper
                )
        );
        return authorizationService;
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository registeredClientRepository
    ) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(AuthorizationProperties properties) {
        return AuthorizationServerSettings.builder()
                .issuer(properties.getIssuer())
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(AuthorizationProperties properties) {
        RSAKey rsaKey = loadConfiguredRsaKey(properties)
                .orElseGet(() -> {
                    if (!properties.getSigningKey().isAllowGeneratedKeyFallback()) {
                        throw new IllegalStateException("인가 서버 서명키가 설정되지 않았습니다.");
                    }
                    return generateRsaKey(properties.getSigningKey().getKeyId());
                });
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            JWKSource<SecurityContext> jwkSource,
            AuthorizationProperties properties,
            ResourceServerProperties resourceServerProperties
    ) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.getIssuer()),
                new ApiAudienceValidator(resourceServerProperties.getHost())
        ));
        return decoder;
    }

    @Bean
    public ApplicationRunner registeredClientSeeder(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository registeredClientRepository,
            AuthorizationProperties properties
    ) {
        return args -> {
            String clientId = properties.getBrowserClient().getClientId();
            if (!authorizationServerTablesReady(jdbcOperations)) {
                return;
            }

            if (registeredClientRepository.findByClientId(clientId) != null) {
                return;
            }

            RegisteredClient registeredClient = RegisteredClient.withId(clientId)
                    .clientId(clientId)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri(properties.getBrowserClient().getRedirectUri())
                    .scope("openid")
                    .scopes(scopes -> scopes.addAll(properties.getBrowserClient().getScopes()))
                    .clientSettings(ClientSettings.builder()
                            .requireProofKey(true)
                            .requireAuthorizationConsent(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofSeconds(properties.getTokens().getAccessTokenExpirationSeconds()))
                            .refreshTokenTimeToLive(Duration.ofSeconds(properties.getTokens().getRefreshTokenExpirationSeconds()))
                            .authorizationCodeTimeToLive(Duration.ofSeconds(properties.getTokens().getAuthorizationCodeExpirationSeconds()))
                            .reuseRefreshTokens(false)
                            .build())
                    .build();

            registeredClientRepository.save(registeredClient);
        };
    }

    private boolean authorizationServerTablesReady(JdbcOperations jdbcOperations) {
        try {
            jdbcOperations.queryForObject("SELECT COUNT(*) FROM oauth2_registered_client", Integer.class);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(ResourceServerProperties resourceServerProperties) {
        return context -> {
            Authentication authentication = context.getPrincipal();
            Object principal = authentication == null ? null : authentication.getPrincipal();
            if (!(principal instanceof AuthenticatedIdentity identityPrincipal)) {
                return;
            }

            if ("access_token".equals(context.getTokenType().getValue())) {
                context.getClaims().audience(List.of(resourceServerProperties.getHost()));
            }

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .toList();

            context.getClaims().subject(identityPrincipal.getName());
            context.getClaims().claim("roles", roles);
            if (identityPrincipal.displayName() != null && !identityPrincipal.displayName().isBlank()) {
                context.getClaims().claim("name", identityPrincipal.displayName());
            }
        };
    }

    private Optional<RSAKey> loadConfiguredRsaKey(AuthorizationProperties properties) {
        String pem = resolvePrivateKeyPem(properties.getSigningKey());
        if (pem == null || pem.isBlank()) {
            return Optional.empty();
        }

        RSAPrivateKey privateKey = parsePrivateKey(pem);
        RSAPublicKey publicKey = derivePublicKey(privateKey);
        return Optional.of(new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(properties.getSigningKey().getKeyId())
                .build());
    }

    private String resolvePrivateKeyPem(AuthorizationProperties.SigningKey signingKey) {
        if (signingKey.getPrivateKeyPath() != null && !signingKey.getPrivateKeyPath().isBlank()) {
            try {
                return Files.readString(Path.of(signingKey.getPrivateKeyPath()));
            } catch (Exception ex) {
                throw new IllegalStateException("인가 서버 private key 파일을 읽을 수 없습니다.", ex);
            }
        }
        return signingKey.getPrivateKeyPem();
    }

    private RSAPrivateKey parsePrivateKey(String privateKeyPem) {
        try {
            String normalized = privateKeyPem
                    .replace("\\n", "\n")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            if (!(privateKey instanceof RSAPrivateKey rsaPrivateKey)) {
                throw new IllegalStateException("RSA private key 형식이 아닙니다.");
            }
            return rsaPrivateKey;
        } catch (Exception ex) {
            throw new IllegalStateException("인가 서버 RSA private key 파싱에 실패했습니다.", ex);
        }
    }

    private RSAPublicKey derivePublicKey(RSAPrivateKey privateKey) {
        if (!(privateKey instanceof RSAPrivateCrtKey crtKey)) {
            throw new IllegalStateException("RSA public key를 private key에서 파생할 수 없습니다.");
        }

        try {
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent());
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("인가 서버 RSA public key 파생에 실패했습니다.", ex);
        }
    }

    private RSAKey generateRsaKey(String keyId) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyId)
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    private String extractHost(String url) {
        return URI.create(url).getHost();
    }

    private JsonMapper authorizationJsonMapper() {
        BasicPolymorphicTypeValidator.Builder validatorBuilder = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(AuthenticatedIdentity.class);

        return JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(
                        AuthorizationConfig.class.getClassLoader(),
                        validatorBuilder
                ))
                .addMixIn(AuthenticatedIdentity.class, AuthenticatedIdentityMixin.class)
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class AuthenticatedIdentityMixin {
    }
}
